/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.content.processor;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Expands whole-map language key placeholders in batch import data.
 *
 * <p>
 * When an entry has a key ending in <code>_i18n</code> and its value is a
 * string matching <code>$LFR_LANGUAGE_KEY-&lt;key&gt;$</code>, the value is
 * replaced with a map of every available locale to its translation.
 * </p>
 *
 * @author Vendel Töreki
 */
@Component(service = LanguageKeyMapExpander.class)
public class LanguageKeyMapExpander {

	public void expand(Map<String, Object> map) {
		if (map == null) {
			return;
		}

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();

			if ((value instanceof String) && _isI18nKey(entry.getKey())) {
				Map<String, String> expanded = _expand((String)value);

				if (expanded != null) {
					entry.setValue(expanded);
				}
			}
			else if (value instanceof Map) {
				expand((Map<String, Object>)value);
			}
			else if (value instanceof List) {
				for (Object element : (List<?>)value) {
					if (element instanceof Map) {
						expand((Map<String, Object>)element);
					}
				}
			}
		}
	}

	private Map<String, String> _expand(String value) {
		Matcher matcher = _pattern.matcher(value);

		if (!matcher.matches()) {
			return null;
		}

		String key = matcher.group(1);

		Set<Locale> availableLocales = _language.getAvailableLocales();

		Map<String, String> result = new HashMap<>();

		for (Locale locale : availableLocales) {
			String resolved = _language.get(locale, key, null);

			if (resolved == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to resolve language key \"", key,
							"\" for locale ", locale));
				}

				continue;
			}

			result.put(LocaleUtil.toLanguageId(locale), resolved);
		}

		if (result.isEmpty()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve whole-map language placeholder ",
						value, " for any locale"));
			}

			return null;
		}

		return result;
	}

	private boolean _isI18nKey(String key) {
		if ((key != null) && key.endsWith("_i18n")) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyMapExpander.class);

	private static final Pattern _pattern = Pattern.compile(
		"^\\$LFR_LANGUAGE_KEY-(.+)\\$$");

	@Reference
	private Language _language;

}