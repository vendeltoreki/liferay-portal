/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.language;

import java.util.Locale;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Vendel Toreki
 */
@ProviderType
public interface LanguageKeyResolver {

	public Map<Locale, String> resolve(Map<Locale, String> localizedMap);

	public String resolve(String value);

}