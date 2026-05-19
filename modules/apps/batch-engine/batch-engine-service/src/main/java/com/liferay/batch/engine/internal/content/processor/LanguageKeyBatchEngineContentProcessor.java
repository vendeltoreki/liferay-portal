/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.content.processor;

import com.liferay.batch.engine.BatchEngineContentProcessor;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Töreki
 */
@Component(service = BatchEngineContentProcessor.class)
public class LanguageKeyBatchEngineContentProcessor
	implements BatchEngineContentProcessor {

	@Override
	public String process(String content) {
		if ((content == null) || !content.contains("$LANG_KEY[")) {
			return content;
		}

		Matcher matcher = _pattern.matcher(content);

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

			String value = _language.get(locale, key, null);

			if (Validator.isNull(value)) {
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

			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LanguageKeyBatchEngineContentProcessor.class);

	private static final Pattern _pattern = Pattern.compile(
		"\\$LANG_KEY\\[([^\\]]+)\\]\\[([^\\]]+)\\]");

	@Reference
	private Language _language;

}