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
	 * Resolves the placeholders in every value of the given localized map.
	 *
	 * @param  localizedMap the locale-keyed map of values to resolve
	 * @return a new localized map with each value resolved
	 */
	public Map<Locale, String> resolve(Map<Locale, String> localizedMap);

}
