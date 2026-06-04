/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
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
			Map<String, String> localizedMap = _getLocalizedMap(item, method);

			if (localizedMap == null) {
				continue;
			}

			Map<String, String> resolvedLocalizedMap = _resolveLocalizedMap(
				localizedMap);

			if (resolvedLocalizedMap == null) {
				continue;
			}

			Method setMethod = _getSetMethod(itemClass, method.getName());

			if (setMethod == null) {
				continue;
			}

			setMethod.invoke(item, resolvedLocalizedMap);
		}
	}

	private Map<String, String> _getLocalizedMap(Object item, Method method) {
		String methodName = method.getName();

		if (!methodName.startsWith("get") ||
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

			Map<String, String> localizedMap = new LinkedHashMap<>();

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				Object entryValue = entry.getValue();

				if ((entry.getKey() instanceof String) &&
					(entryValue instanceof String)) {

					localizedMap.put(
						(String)entry.getKey(), (String)entryValue);
				}
				else {
					return null;
				}
			}

			return localizedMap;
		}
		catch (ReflectiveOperationException reflectiveOperationException) {
			if (_log.isDebugEnabled()) {
				_log.debug(reflectiveOperationException);
			}

			return null;
		}
	}

	private Method _getSetMethod(Class<?> itemClass, String getMethodName) {
		try {
			return itemClass.getMethod(
				"set" + getMethodName.substring(3), Map.class);
		}
		catch (NoSuchMethodException noSuchMethodException) {
			if (_log.isDebugEnabled()) {
				_log.debug(noSuchMethodException);
			}

			return null;
		}
	}

	private Map<String, String> _resolveLocalizedMap(
		Map<String, String> localizedMap) {

		Map<Locale, String> localeMap = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : localizedMap.entrySet()) {
			localeMap.put(
				LocaleUtil.fromLanguageId(entry.getKey(), false),
				entry.getValue());
		}

		Map<Locale, String> resolvedLocaleMap = _languageKeyResolver.resolve(
			localeMap);

		if (resolvedLocaleMap.equals(localeMap)) {
			return null;
		}

		Map<String, String> resolvedLocalizedMap = new LinkedHashMap<>();

		for (Map.Entry<Locale, String> entry : resolvedLocaleMap.entrySet()) {
			resolvedLocalizedMap.put(
				LocaleUtil.toLanguageId(entry.getKey()), entry.getValue());
		}

		return resolvedLocalizedMap;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyImportTaskPreAction.class);

	@Reference
	private LanguageKeyResolver _languageKeyResolver;

}