/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.action.ImportTaskPreAction;
import com.liferay.batch.engine.context.ImportTaskContext;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.lang.reflect.Method;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resolves <code>$LANG_KEY[key][locale]</code> placeholders embedded in the
 * values of localized (<code>*_i18n</code>) fields of an imported item. The
 * placeholder is replaced inline with the translation that {@link Language}
 * returns for the given key and locale before the item is persisted.
 *
 * @author Vendel Toreki
 */
@Component(service = ImportTaskPreAction.class)
public class LanguageKeyImportTaskPreAction implements ImportTaskPreAction {

	@Override
	public void run(
			BatchEngineImportTask batchEngineImportTask,
			BatchEngineTaskItemDelegate<?> batchEngineTaskItemDelegate,
			ImportTaskContext importTaskContext, Object item)
		throws Exception {

		if (item == null) {
			return;
		}

		Class<?> itemClass = item.getClass();

		for (Method method : itemClass.getMethods()) {
			Map<String, String> i18nMap = _getI18nMap(item, method);

			if (i18nMap == null) {
				continue;
			}

			Map<String, String> resolvedI18nMap = _resolveI18nMap(
				batchEngineImportTask, _getFieldName(method), i18nMap);

			if (resolvedI18nMap == null) {
				continue;
			}

			String methodNameSuffix = method.getName(
			).substring(
				3
			);

			Method setMethod = itemClass.getMethod(
				"set" + methodNameSuffix, Map.class);

			setMethod.invoke(item, resolvedI18nMap);
		}
	}

	private String _getFieldName(Method method) {

		// Strip the leading "get" so logs reference the field as authored in
		// the batch file (for example "name_i18n").

		String fieldName = method.getName(
		).substring(
			3
		);

		return Character.toLowerCase(fieldName.charAt(0)) +
			fieldName.substring(1);
	}

	private Map<String, String> _getI18nMap(Object item, Method method) {
		String methodName = method.getName();

		if (!methodName.startsWith("get") || !methodName.endsWith("_i18n") ||
			(method.getParameterCount() != 0) ||
			!Map.class.isAssignableFrom(method.getReturnType())) {

			return null;
		}

		try {
			Object value = method.invoke(item);

			if (!(value instanceof Map)) {
				return null;
			}

			Map<?, ?> map = (Map<?, ?>)value;

			if (map.isEmpty()) {
				return null;
			}

			Map<String, String> i18nMap = new LinkedHashMap<>();

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				Object entryValue = entry.getValue();

				if ((entry.getKey() instanceof String) &&
					(entryValue instanceof String)) {

					i18nMap.put((String)entry.getKey(), (String)entryValue);
				}
				else {

					// A non-string entry is not a localization value this
					// action understands; leave the field untouched.

					return null;
				}
			}

			return i18nMap;
		}
		catch (ReflectiveOperationException reflectiveOperationException) {
			if (_log.isDebugEnabled()) {
				_log.debug(reflectiveOperationException);
			}

			return null;
		}
	}

	private Locale _getLocale(
		BatchEngineImportTask batchEngineImportTask, String languageId) {

		for (Locale locale :
				_language.getAvailableLocales(
					batchEngineImportTask.getCompanyId())) {

			// Locale matching is case sensitive: the placeholder must spell the
			// language ID exactly as the portal expects (for example "en_US",
			// not "en_us"), otherwise it is treated as an unknown locale.

			if (languageId.equals(LocaleUtil.toLanguageId(locale))) {
				return locale;
			}
		}

		return null;
	}

	private boolean _hasWhitespace(String value) {
		for (int i = 0; i < value.length(); i++) {
			if (Character.isWhitespace(value.charAt(i))) {
				return true;
			}
		}

		return false;
	}

	private void _logMalformed(
		BatchEngineImportTask batchEngineImportTask, String fieldName,
		String placeholder, String reason) {

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Leaving malformed language key placeholder \"",
					placeholder, "\" (", reason, ") unchanged in field \"",
					fieldName, "\" of batch engine import task ",
					batchEngineImportTask.getBatchEngineImportTaskId()));
		}
	}

	private void _logUnresolved(
		BatchEngineImportTask batchEngineImportTask, String fieldName,
		String key, String languageId, String reason) {

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Unable to resolve language key \"", key,
					"\" for locale \"", languageId, "\" (", reason,
					") in field \"", fieldName,
					"\" of batch engine import task ",
					batchEngineImportTask.getBatchEngineImportTaskId(),
					"; leaving the value empty"));
		}
	}

	private String _resolve(
		BatchEngineImportTask batchEngineImportTask, String fieldName,
		Matcher matcher) {

		String key = matcher.group(1);
		String languageId = matcher.group(2);

		// A placeholder missing either bracket group is malformed; warn and
		// leave it unchanged.

		if ((key == null) || (languageId == null)) {
			_logMalformed(
				batchEngineImportTask, fieldName, matcher.group(),
				"missing key or locale bracket");

			return matcher.group();
		}

		// Whitespace inside the brackets is not part of the syntax; treat the
		// value as a literal string and leave it unchanged without warning.

		if (_hasWhitespace(key) || _hasWhitespace(languageId)) {
			return matcher.group();
		}

		if (key.isEmpty()) {
			_logMalformed(
				batchEngineImportTask, fieldName, matcher.group(), "empty key");

			return matcher.group();
		}

		if (!_languageIdPattern.matcher(
				languageId
			).matches()) {

			_logMalformed(
				batchEngineImportTask, fieldName, matcher.group(),
				"unrecognized locale format");

			return matcher.group();
		}

		Locale locale = _getLocale(batchEngineImportTask, languageId);

		if (locale == null) {
			_logUnresolved(
				batchEngineImportTask, fieldName, key, languageId,
				"unknown locale");

			return StringPool.BLANK;
		}

		String value = _language.get(locale, key);

		// Language returns the key itself when no translation exists.

		if ((value == null) || value.equals(key)) {
			_logUnresolved(
				batchEngineImportTask, fieldName, key, languageId,
				"unknown key");

			return StringPool.BLANK;
		}

		return value;
	}

	private Map<String, String> _resolveI18nMap(
		BatchEngineImportTask batchEngineImportTask, String fieldName,
		Map<String, String> i18nMap) {

		Map<String, String> resolvedI18nMap = new LinkedHashMap<>();

		boolean changed = false;

		for (Map.Entry<String, String> entry : i18nMap.entrySet()) {
			String value = entry.getValue();

			String resolvedValue = _resolveValue(
				batchEngineImportTask, fieldName, value);

			resolvedI18nMap.put(entry.getKey(), resolvedValue);

			if (!Objects.equals(value, resolvedValue)) {
				changed = true;
			}
		}

		if (!changed) {
			return null;
		}

		return resolvedI18nMap;
	}

	private String _resolveValue(
		BatchEngineImportTask batchEngineImportTask, String fieldName,
		String value) {

		if (Validator.isNull(value) || !value.contains(_LANGUAGE_KEY_PREFIX)) {
			return value;
		}

		Matcher matcher = _placeholderPattern.matcher(value);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(
				sb,
				Matcher.quoteReplacement(
					_resolve(batchEngineImportTask, fieldName, matcher)));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static final String _LANGUAGE_KEY_PREFIX = "$LANG_KEY";

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyImportTaskPreAction.class);

	// A locale must look like a language ID (for example "en" or "en_US"). The
	// shape is matched case insensitively here so that a case mismatch falls
	// through to the case-sensitive lookup and is reported as an unknown
	// locale rather than a malformed one.

	private static final Pattern _languageIdPattern = Pattern.compile(
		"[A-Za-z]{2,3}(_[A-Za-z]{2,4})?");

	// Captures the optional bracket groups of a "$LANG_KEY[key][locale]"
	// placeholder. The groups are optional so a malformed placeholder missing a
	// bracket is still matched and can be reported. Bracket content excludes
	// brackets only, so whitespace is captured and detected afterwards.

	private static final Pattern _placeholderPattern = Pattern.compile(
		"\\$LANG_KEY(?:\\[([^\\[\\]]*)\\])?(?:\\[([^\\[\\]]*)\\])?");

	@Reference
	private Language _language;

}