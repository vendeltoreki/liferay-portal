/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.action.ImportTaskPreAction;
import com.liferay.batch.engine.context.ImportTaskContext;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.lang.reflect.Method;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resolves Liferay language key placeholders in localized fields of an
 * imported batch item.
 *
 * <p>
 * The supported placeholder form is per-locale:
 * <code>$LANG_KEY[&lt;key&gt;][&lt;locale&gt;]</code>. Any string value of an
 * <code>*_i18n</code> map on the item is scanned for the pattern; matches are
 * replaced with the translation of <code>key</code> for the named locale.
 * Unresolved placeholders are left untouched and a warning is logged with the
 * key, the locale, the offending field, and the originating batch task.
 * </p>
 *
 * @author Vendel Töreki
 */
@Component(service = ImportTaskPreAction.class)
public class LanguageKeyImportTaskPreAction implements ImportTaskPreAction {

	@Override
	public void run(
		BatchEngineImportTask batchEngineImportTask,
		BatchEngineTaskItemDelegate<?> batchEngineTaskItemDelegate,
		ImportTaskContext importTaskContext, Object item) {

		if (item == null) {
			return;
		}

		Class<?> itemClass = item.getClass();

		for (Method method : itemClass.getMethods()) {
			if (!_isI18nGetter(method)) {
				continue;
			}

			try {
				Map<String, String> i18nMap =
					(Map<String, String>)method.invoke(item);

				_resolveI18nMap(
					i18nMap, _toFieldName(method.getName()),
					batchEngineImportTask);
			}
			catch (Exception exception) {
				_log.error(
					"Unable to resolve language keys on field " +
						method.getName(),
					exception);
			}
		}
	}

	private boolean _isI18nGetter(Method method) {
		String name = method.getName();

		if (!name.startsWith("get") || !name.endsWith("_i18n") ||
			(method.getParameterCount() != 0) ||
			!Map.class.isAssignableFrom(method.getReturnType())) {

			return false;
		}

		return true;
	}

	private void _resolveI18nMap(
		Map<String, String> i18nMap, String fieldName,
		BatchEngineImportTask batchEngineImportTask) {

		if ((i18nMap == null) || i18nMap.isEmpty()) {
			return;
		}

		for (Map.Entry<String, String> entry : i18nMap.entrySet()) {
			String resolved = _resolveValue(
				entry.getValue(), fieldName, batchEngineImportTask);

			if (!resolved.equals(entry.getValue())) {
				entry.setValue(resolved);
			}
		}
	}

	private String _resolveValue(
		String value, String fieldName,
		BatchEngineImportTask batchEngineImportTask) {

		if ((value == null) || !value.contains("$LANG_KEY[")) {
			return value;
		}

		Matcher matcher = _pattern.matcher(value);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String key = matcher.group(1);

			String languageId = matcher.group(2);

			Locale locale = LocaleUtil.fromLanguageId(languageId, false, false);

			if (locale == null) {
				_warnUnresolved(
					"unknown locale", key, languageId, fieldName,
					batchEngineImportTask);

				matcher.appendReplacement(
					sb, Matcher.quoteReplacement(matcher.group()));

				continue;
			}

			String translation = _language.get(locale, key, null);

			if (Validator.isNull(translation)) {
				_warnUnresolved(
					"missing translation", key, languageId, fieldName,
					batchEngineImportTask);

				matcher.appendReplacement(
					sb, Matcher.quoteReplacement(matcher.group()));

				continue;
			}

			matcher.appendReplacement(
				sb, Matcher.quoteReplacement(translation));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private String _toFieldName(String getterName) {
		String name = getterName.substring(3);

		if (name.isEmpty()) {
			return getterName;
		}

		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	private void _warnUnresolved(
		String reason, String key, String languageId, String fieldName,
		BatchEngineImportTask batchEngineImportTask) {

		if (!_log.isWarnEnabled()) {
			return;
		}

		_log.warn(
			StringBundler.concat(
				"Unable to resolve language placeholder for key \"", key,
				"\" locale \"", languageId, "\" on field \"", fieldName,
				"\" of batch engine import task ",
				batchEngineImportTask.getBatchEngineImportTaskId(), ": ",
				reason));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyImportTaskPreAction.class);

	private static final Pattern _pattern = Pattern.compile(
		"\\$LANG_KEY\\[([^\\]]+)\\]\\[([^\\]]+)\\]");

	@Reference
	private Language _language;

}