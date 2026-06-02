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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

		Map<Locale, String> resolvedLocalizedMap = new LinkedHashMap<>();

		List<String> expansionKeys = new ArrayList<>();

		// Resolve per-locale placeholders inline and record the keys of any
		// full-expansion placeholders for a second pass, so that explicit
		// entries are in place before the expansion fills in the rest.

		for (Map.Entry<Locale, String> entry : localizedMap.entrySet()) {
			String expansionKey = _getExpansionKey(entry.getValue());

			if (expansionKey == null) {
				resolvedLocalizedMap.put(
					entry.getKey(), resolve(entry.getValue()));
			}
			else {
				expansionKeys.add(expansionKey);
			}
		}

		for (String expansionKey : expansionKeys) {
			Map<Locale, String> expandedLocalizedMap = _expand(expansionKey);

			if (expandedLocalizedMap.isEmpty()) {
				_logUnexpanded(expansionKey);

				continue;
			}

			// Pre-existing locale entries are preserved and not overwritten.

			for (Map.Entry<Locale, String> entry :
					expandedLocalizedMap.entrySet()) {

				resolvedLocalizedMap.putIfAbsent(
					entry.getKey(), entry.getValue());
			}
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

	private String _getExpansionKey(String value) {
		if (Validator.isNull(value) ||
			!value.contains(_FULL_EXPANSION_PREFIX)) {

			return null;
		}

		Matcher matcher = _fullExpansionPattern.matcher(value);

		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
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

	private void _logUnexpanded(String key) {
		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Unable to expand language key \"", key,
					"\" to any locale translation; leaving the value empty"));
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

	private static final String _FULL_EXPANSION_PREFIX = "[$LFR_LANGUAGE_KEY-";

	private static final String _LANGUAGE_KEY_PREFIX = "$LANG_KEY";

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyResolverImpl.class);

	// Matches a full-expansion placeholder "[$LFR_LANGUAGE_KEY-key$]" that is
	// the entire value of an _i18n map entry, capturing the key name.

	private static final Pattern _fullExpansionPattern = Pattern.compile(
		"\\[\\$LFR_LANGUAGE_KEY-([\\w.-]+)\\$\\]");

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