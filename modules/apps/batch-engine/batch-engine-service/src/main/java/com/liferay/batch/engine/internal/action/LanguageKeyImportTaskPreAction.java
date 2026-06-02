/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.action.ImportTaskPreAction;
import com.liferay.batch.engine.context.ImportTaskContext;
import com.liferay.batch.engine.language.LanguageKeyResolver;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.lang.reflect.Method;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resolves <code>$LANG_KEY[key][locale]</code> placeholders embedded in the
 * values of localized (<code>*_i18n</code>) fields of an imported item before
 * the item is persisted. The actual placeholder resolution is delegated to
 * {@link LanguageKeyResolver}, which is shared with the site initializer
 * framework so both honor the syntax identically.
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

			Map<String, String> resolvedI18nMap = _resolveI18nMap(i18nMap);

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

	private Map<String, String> _resolveI18nMap(Map<String, String> i18nMap) {

		// The shared resolver works on locale-keyed maps. Convert the item's
		// language-id keys to locales, resolve, and convert back. A full
		// expansion can add locales, so the resolved map is compared as a whole
		// to decide whether the field changed.

		Map<Locale, String> localizedMap = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : i18nMap.entrySet()) {
			localizedMap.put(
				LocaleUtil.fromLanguageId(entry.getKey(), false),
				entry.getValue());
		}

		Map<Locale, String> resolvedLocalizedMap = _languageKeyResolver.resolve(
			localizedMap);

		if (resolvedLocalizedMap.equals(localizedMap)) {
			return null;
		}

		Map<String, String> resolvedI18nMap = new LinkedHashMap<>();

		for (Map.Entry<Locale, String> entry :
				resolvedLocalizedMap.entrySet()) {

			resolvedI18nMap.put(
				LocaleUtil.toLanguageId(entry.getKey()), entry.getValue());
		}

		return resolvedI18nMap;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyImportTaskPreAction.class);

	@Reference
	private LanguageKeyResolver _languageKeyResolver;

}