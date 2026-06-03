/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.language;

import com.liferay.batch.engine.language.LanguageKeyResolver;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
 */
@Component(service = LanguageKeyResolver.class)
public class LanguageKeyResolverImpl implements LanguageKeyResolver {

	@Override
	public Map<Locale, String> resolve(Map<Locale, String> localizedMap) {
		if ((localizedMap == null) || localizedMap.isEmpty()) {
			return localizedMap;
		}

		// A localized map made up of a single en_US entry whose value is a
		// language key is expanded to every locale that has a translation for
		// that key. A value that is not a known key is left untouched.

		if (localizedMap.size() == 1) {
			String key = localizedMap.get(LocaleUtil.US);

			if (key != null) {
				Map<Locale, String> expandedLocalizedMap = _expand(key);

				if (!expandedLocalizedMap.isEmpty()) {
					return expandedLocalizedMap;
				}
			}
		}

		Map<Locale, String> resolvedLocalizedMap = new LinkedHashMap<>();

		for (Map.Entry<Locale, String> entry : localizedMap.entrySet()) {
			resolvedLocalizedMap.put(entry.getKey(), resolve(entry.getValue()));
		}

		return resolvedLocalizedMap;
	}

	@Override
	public String resolve(String value) {
		if (Validator.isNull(value) || !value.contains(_LANGUAGE_KEY_PREFIX)) {
			return value;
		}

		Matcher matcher = _placeholderPattern.matcher(value);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(
				sb, Matcher.quoteReplacement(_resolve(matcher)));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private Map<Locale, String> _expand(String key) {
		Map<Locale, String> expandedLocalizedMap = new LinkedHashMap<>();

		for (Locale locale : _language.getAvailableLocales()) {
			String value = _language.get(locale, key);

			// Only locales that have their own non-empty translation are
			// included; Language returns the key itself when none exists.

			if (!Validator.isBlank(value) && !value.equals(key)) {
				expandedLocalizedMap.put(locale, value);
			}
		}

		return expandedLocalizedMap;
	}

	private Locale _getLocale(String languageId) {
		for (Locale locale : _language.getAvailableLocales()) {

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

	private void _logMalformed(String placeholder, String reason) {
		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Leaving malformed language key placeholder \"",
					placeholder, "\" (", reason, ") unchanged"));
		}
	}

	private void _logUnresolved(String key, String languageId, String reason) {
		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Unable to resolve language key \"", key,
					"\" for locale \"", languageId, "\" (", reason,
					"); leaving the value empty"));
		}
	}

	private String _resolve(Matcher matcher) {
		String key = matcher.group(1);
		String languageId = matcher.group(2);

		// A placeholder missing either bracket group is malformed; warn and
		// leave it unchanged.

		if ((key == null) || (languageId == null)) {
			_logMalformed(matcher.group(), "missing key or locale bracket");

			return matcher.group();
		}

		// Whitespace inside the brackets is not part of the syntax; treat the
		// value as a literal string and leave it unchanged without warning.

		if (_hasWhitespace(key) || _hasWhitespace(languageId)) {
			return matcher.group();
		}

		if (key.isEmpty()) {
			_logMalformed(matcher.group(), "empty key");

			return matcher.group();
		}

		if (!_languageIdPattern.matcher(
				languageId
			).matches()) {

			_logMalformed(matcher.group(), "unrecognized locale format");

			return matcher.group();
		}

		Locale locale = _getLocale(languageId);

		if (locale == null) {
			_logUnresolved(key, languageId, "unknown locale");

			return StringPool.BLANK;
		}

		String value = _language.get(locale, key);

		// Language returns the key itself when no translation exists.

		if ((value == null) || value.equals(key)) {
			_logUnresolved(key, languageId, "unknown key");

			return StringPool.BLANK;
		}

		return value;
	}

	private static final String _LANGUAGE_KEY_PREFIX = "$LANG_KEY";

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyResolverImpl.class);

	// A locale must look like a language ID (for example "en" or "en_US"). The
	// shape is matched case insensitively here so that a case mismatch falls
	// through to the case-sensitive lookup and is reported as an unknown locale
	// rather than a malformed one.

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