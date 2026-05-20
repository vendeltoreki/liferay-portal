/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.initializer.extender;

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
public interface LanguageKeyJSONResolver {

	public String resolve(String json);

}