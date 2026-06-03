/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.language;

import java.util.Locale;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Resolves <code>$LANG_KEY[key][locale]</code> placeholders to the translation
 * that the portal returns for the given key and locale. The same resolution is
 * shared by Batch Engine imports and by site initializers so that both honor
 * the placeholder syntax identically.
 *
 * @author Vendel Toreki
 */
@ProviderType
public interface LanguageKeyResolver {

	/**
	 * Resolves every <code>$LANG_KEY[key][locale]</code> placeholder embedded in
	 * the given value, replacing each one inline with its translation. Unknown
	 * keys or locales resolve to an empty string; malformed or whitespace-padded
	 * placeholders are left untouched.
	 *
	 * @param  value the value possibly containing placeholders
	 * @return the value with its placeholders resolved
	 */
	public String resolve(String value);

	/**
	 * Resolves a localized map. A map made up of a single <code>en_US</code>
	 * entry whose value is a language key is expanded to every locale that has
	 * a translation for that key; otherwise every value is resolved inline as
	 * with {@link #resolve(String)}.
	 *
	 * @param  localizedMap the locale-keyed map of values to resolve
	 * @return a new localized map with its values resolved or expanded
	 */
	public Map<Locale, String> resolve(Map<Locale, String> localizedMap);

}
