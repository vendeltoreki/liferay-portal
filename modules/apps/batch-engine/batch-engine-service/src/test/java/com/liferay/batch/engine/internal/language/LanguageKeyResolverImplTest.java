/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.language;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Vendel Toreki
 */
public class LanguageKeyResolverImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_languageKeyResolverImpl = new LanguageKeyResolverImpl();

		Field field = LanguageKeyResolverImpl.class.getDeclaredField(
			"_language");

		field.setAccessible(true);

		field.set(_languageKeyResolverImpl, _createLanguage());
	}

	@Test
	public void testEmbeddedPlaceholderResolvedInline() {

		// AC4

		Assert.assertEquals(
			"prefix Welcome suffix",
			_languageKeyResolverImpl.resolve(
				"prefix $LANG_KEY[welcome][en_US] suffix"));
	}

	@Test
	public void testFullExpansionExcludesLocalesWithoutTranslation() {

		// AC4, AC5

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "[$LFR_LANGUAGE_KEY-greeting$]"
				).build());

		Assert.assertEquals(
			resolvedLocalizedMap.toString(), 1, resolvedLocalizedMap.size());
		Assert.assertEquals("Hi", resolvedLocalizedMap.get(LocaleUtil.US));
	}

	@Test
	public void testFullExpansionMixedWithPerLocalePlaceholder() {

		// AC7

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "$LANG_KEY[welcome][en_US]"
				).put(
					LocaleUtil.SPAIN, "[$LFR_LANGUAGE_KEY-welcome$]"
				).build());

		Assert.assertEquals("Welcome", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"Bienvenido", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testFullExpansionPreservesExistingEntries() {

		// AC6

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "Custom"
				).put(
					LocaleUtil.SPAIN, "[$LFR_LANGUAGE_KEY-welcome$]"
				).build());

		Assert.assertEquals("Custom", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"Bienvenido", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testFullExpansionToAllLocales() {

		// AC1, AC4

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "[$LFR_LANGUAGE_KEY-welcome$]"
				).build());

		Assert.assertEquals("Welcome", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"Bienvenido", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testFullExpansionUnknownKeyLeftEmpty() {

		// AC2

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "[$LFR_LANGUAGE_KEY-missing$]"
				).build());

		Assert.assertTrue(
			resolvedLocalizedMap.toString(), resolvedLocalizedMap.isEmpty());
	}

	@Test
	public void testLocalizedMapResolvedPerValue() {

		// AC1

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "$LANG_KEY[welcome][en_US]"
				).put(
					LocaleUtil.SPAIN, "$LANG_KEY[welcome][es_ES]"
				).build());

		Assert.assertEquals("Welcome", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"Bienvenido", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testMalformedEmptyKeyLeftUnchanged() {

		// AC5

		Assert.assertEquals(
			"$LANG_KEY[][en_US]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[][en_US]"));
	}

	@Test
	public void testMalformedLocaleFormatLeftUnchanged() {

		// AC5

		Assert.assertEquals(
			"$LANG_KEY[welcome][en-US]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en-US]"));
	}

	@Test
	public void testMalformedMissingLocaleBracketLeftUnchanged() {

		// AC5

		Assert.assertEquals(
			"$LANG_KEY[welcome]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome]"));
	}

	@Test
	public void testUnknownKeyLeftEmpty() {

		// AC2

		Assert.assertEquals(
			"", _languageKeyResolverImpl.resolve("$LANG_KEY[missing][en_US]"));
	}

	@Test
	public void testUnknownLocaleCaseMismatchLeftEmpty() {

		// AC2, AC6

		Assert.assertEquals(
			"", _languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en_us]"));
	}

	@Test
	public void testValidPlaceholderResolved() {

		// AC1

		Assert.assertEquals(
			"Welcome",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en_US]"));
	}

	@Test
	public void testWhitespaceInBracketsLeftUnchanged() {

		// AC7

		Assert.assertEquals(
			"$LANG_KEY[ welcome ][ en_US ]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[ welcome ][ en_US ]"));
	}

	private Language _createLanguage() {
		Map<String, String> translations = HashMapBuilder.put(
			"en_US/greeting", "Hi"
		).put(
			"en_US/welcome", "Welcome"
		).put(
			"es_ES/welcome", "Bienvenido"
		).build();

		Set<Locale> availableLocales = new LinkedHashSet<>();

		availableLocales.add(LocaleUtil.US);
		availableLocales.add(LocaleUtil.SPAIN);

		return (Language)ProxyUtil.newProxyInstance(
			Language.class.getClassLoader(), new Class<?>[] {Language.class},
			new InvocationHandler() {

				@Override
				public Object invoke(
					Object proxy, Method method, Object[] args) {

					String name = method.getName();

					if (name.equals("getAvailableLocales") && (args == null)) {
						return availableLocales;
					}

					if (name.equals("get") && (args.length == 2) &&
						(args[0] instanceof Locale)) {

						String key = (String)args[1];

						String translation = translations.get(
							LocaleUtil.toLanguageId((Locale)args[0]) + "/" +
								key);

						// Language returns the key itself when no translation
						// exists.

						if (translation == null) {
							return key;
						}

						return translation;
					}

					return null;
				}

			});
	}

	private LanguageKeyResolverImpl _languageKeyResolverImpl;

}