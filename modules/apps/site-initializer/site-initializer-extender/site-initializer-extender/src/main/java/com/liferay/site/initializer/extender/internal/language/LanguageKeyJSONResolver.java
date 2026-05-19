/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.initializer.extender.internal.language;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resolves Liferay language key placeholders inside a JSON document loaded by
 * the site initializer.
 *
 * <p>
 * Two placeholder forms are recognized:
 * </p>
 *
 * <dl>
 *     <dt><code>$LANG_KEY[&lt;key&gt;][&lt;locale&gt;]</code></dt>
 *     <dd>
 *         Per-locale substitution inside any string value. Replaced with the
 *         translation of <code>key</code> for the named locale.
 *     </dd>
 *     <dt><code>$LFR_LANGUAGE_KEY-&lt;key&gt;$</code></dt>
 *     <dd>
 *         Whole-map expansion. When the entire value of an entry whose key
 *         ends in <code>_i18n</code> matches this pattern, it is replaced by a
 *         JSON object mapping each available locale to its translation.
 *     </dd>
 * </dl>
 *
 * @author Vendel Töreki
 */
@Component(service = {})
public class LanguageKeyJSONResolver {

	public String resolve(String json) {
		if (Validator.isNull(json) ||
			(!json.contains("$LANG_KEY[") &&
			 !json.contains("$LFR_LANGUAGE_KEY-"))) {

			return json;
		}

		try {
			String trimmed = json.trim();

			if (trimmed.startsWith("[")) {
				JSONArray jsonArray = _resolve(
					_jsonFactory.createJSONArray(json));

				return jsonArray.toString();
			}

			JSONObject jsonObject = _jsonFactory.createJSONObject(json);

			_resolve(jsonObject);

			return jsonObject.toString();
		}
		catch (JSONException jsonException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to parse JSON for language key resolution, " +
						"returning unchanged",
					jsonException);
			}

			return json;
		}
	}

	private JSONObject _expandWholeMap(String value) {
		Matcher matcher = _wholeMapPattern.matcher(value);

		if (!matcher.matches()) {
			return null;
		}

		String key = matcher.group(1);

		Set<Locale> availableLocales = _language.getAvailableLocales();

		JSONObject jsonObject = _jsonFactory.createJSONObject();

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

			jsonObject.put(LocaleUtil.toLanguageId(locale), resolved);
		}

		if (JSONUtil.isEmpty(jsonObject)) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve whole-map language placeholder ",
						value, " for any locale"));
			}

			return null;
		}

		return jsonObject;
	}

	private boolean _isI18nKey(String key) {
		if ((key != null) && key.endsWith("_i18n")) {
			return true;
		}

		return false;
	}

	private JSONArray _resolve(JSONArray jsonArray) {
		JSONArray resultJSONArray = _jsonFactory.createJSONArray();

		for (int i = 0; i < jsonArray.length(); i++) {
			Object element = jsonArray.get(i);

			if (element instanceof JSONObject) {
				_resolve((JSONObject)element);

				resultJSONArray.put((JSONObject)element);
			}
			else if (element instanceof JSONArray) {
				resultJSONArray.put(_resolve((JSONArray)element));
			}
			else if (element instanceof String) {
				resultJSONArray.put(_resolvePerLocale((String)element));
			}
			else {
				resultJSONArray.put(element);
			}
		}

		return resultJSONArray;
	}

	private void _resolve(JSONObject jsonObject) {
		Iterator<String> keysIterator = jsonObject.keys();

		while (keysIterator.hasNext()) {
			String key = keysIterator.next();

			Object value = jsonObject.get(key);

			if ((value instanceof String) && _isI18nKey(key)) {
				JSONObject expandedJSONObject = _expandWholeMap((String)value);

				if (expandedJSONObject != null) {
					jsonObject.put(key, expandedJSONObject);

					continue;
				}
			}

			if (value instanceof String) {
				jsonObject.put(key, _resolvePerLocale((String)value));
			}
			else if (value instanceof JSONObject) {
				_resolve((JSONObject)value);
			}
			else if (value instanceof JSONArray) {
				jsonObject.put(key, _resolve((JSONArray)value));
			}
		}
	}

	private String _resolvePerLocale(String content) {
		if ((content == null) || !content.contains("$LANG_KEY[")) {
			return content;
		}

		Matcher matcher = _perLocalePattern.matcher(content);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String key = matcher.group(1);

			String languageId = matcher.group(2);

			Locale locale = LocaleUtil.fromLanguageId(languageId, false, false);

			if (locale == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to resolve language placeholder ",
							matcher.group(), ": unknown locale ", languageId));
				}

				matcher.appendReplacement(
					sb, Matcher.quoteReplacement(matcher.group()));

				continue;
			}

			String resolved = _language.get(locale, key, null);

			if (Validator.isNull(resolved)) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to resolve language key \"", key,
							"\" for locale ", languageId));
				}

				matcher.appendReplacement(
					sb, Matcher.quoteReplacement(matcher.group()));

				continue;
			}

			matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyJSONResolver.class);

	private static final Pattern _perLocalePattern = Pattern.compile(
		"\\$LANG_KEY\\[([^\\]]+)\\]\\[([^\\]]+)\\]");
	private static final Pattern _wholeMapPattern = Pattern.compile(
		"^\\$LFR_LANGUAGE_KEY-(.+)\\$$");

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}