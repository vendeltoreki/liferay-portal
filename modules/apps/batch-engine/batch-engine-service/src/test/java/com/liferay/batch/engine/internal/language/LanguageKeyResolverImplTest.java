/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.language;

import com.liferay.batch.engine.configuration.BatchEngineTaskCompanyConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
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

		_setField("_language", _createLanguage());

		_setConfigurationProvider(true);
	}

	@Test
	public void testEmbeddedPlaceholderResolvedInline() {

		// LPD-88511 AC4

		Assert.assertEquals(
			"prefix Welcome suffix",
			_languageKeyResolverImpl.resolve(
				"prefix $LANG_KEY[welcome][en_US] suffix"));
	}

	@Test
	public void testLocalizedMapResolvedPerValue() {

		// LPD-88511 AC1

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

		// LPD-88511 AC5

		Assert.assertEquals(
			"$LANG_KEY[][en_US]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[][en_US]"));
	}

	@Test
	public void testMalformedLocaleFormatLeftUnchanged() {

		// LPD-88511 AC5

		Assert.assertEquals(
			"$LANG_KEY[welcome][en-US]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en-US]"));
	}

	@Test
	public void testMalformedMissingLocaleBracketLeftUnchanged() {

		// LPD-88511 AC5

		Assert.assertEquals(
			"$LANG_KEY[welcome]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome]"));
	}

	@Test
	public void testMultipleEntriesNotExpanded() {
		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "welcome"
				).put(
					LocaleUtil.SPAIN, "other"
				).build());

		Assert.assertEquals("welcome", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"other", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testResolutionSkippedWhenDisabled() throws Exception {

		// LPD-88510

		_setConfigurationProvider(false);

		Assert.assertEquals(
			"$LANG_KEY[welcome][en_US]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en_US]"));

		Map<Locale, String> localizedMap = LinkedHashMapBuilder.put(
			LocaleUtil.US, "welcome"
		).build();

		Assert.assertEquals(
			localizedMap, _languageKeyResolverImpl.resolve(localizedMap));
	}

	@Test
	public void testSingleEnUSEntryExpandedToAllLocales() {

		// LPD-88512 AC1

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "welcome"
				).build());

		Assert.assertEquals("Welcome", resolvedLocalizedMap.get(LocaleUtil.US));
		Assert.assertEquals(
			"Bienvenido", resolvedLocalizedMap.get(LocaleUtil.SPAIN));
	}

	@Test
	public void testSingleEnUSEntryExpandedToTranslatedLocalesOnly() {

		// LPD-88512 AC4, AC5

		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "greeting"
				).build());

		Assert.assertEquals(
			resolvedLocalizedMap.toString(), 1, resolvedLocalizedMap.size());
		Assert.assertEquals("Hi", resolvedLocalizedMap.get(LocaleUtil.US));
	}

	@Test
	public void testSingleEnUSEntryWithLiteralValueLeftUnchanged() {
		Map<Locale, String> resolvedLocalizedMap =
			_languageKeyResolverImpl.resolve(
				LinkedHashMapBuilder.put(
					LocaleUtil.US, "Custom Title"
				).build());

		Assert.assertEquals(
			resolvedLocalizedMap.toString(), 1, resolvedLocalizedMap.size());
		Assert.assertEquals(
			"Custom Title", resolvedLocalizedMap.get(LocaleUtil.US));
	}

	@Test
	public void testUnknownKeyLeftEmpty() {

		// LPD-88511 AC2

		Assert.assertEquals(
			"", _languageKeyResolverImpl.resolve("$LANG_KEY[missing][en_US]"));
	}

	@Test
	public void testUnknownLocaleCaseMismatchLeftEmpty() {

		// LPD-88511 AC2, AC6

		Assert.assertEquals(
			"", _languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en_us]"));
	}

	@Test
	public void testValidPlaceholderResolved() {

		// LPD-88511 AC1

		Assert.assertEquals(
			"Welcome",
			_languageKeyResolverImpl.resolve("$LANG_KEY[welcome][en_US]"));
	}

	@Test
	public void testWhitespaceInBracketsLeftUnchanged() {

		// LPD-88511 AC7

		Assert.assertEquals(
			"$LANG_KEY[ welcome ][ en_US ]",
			_languageKeyResolverImpl.resolve("$LANG_KEY[ welcome ][ en_US ]"));
	}

	private ConfigurationProvider _createConfigurationProvider(
		boolean enabled) {

		BatchEngineTaskCompanyConfiguration
			batchEngineTaskCompanyConfiguration =
				(BatchEngineTaskCompanyConfiguration)ProxyUtil.newProxyInstance(
					BatchEngineTaskCompanyConfiguration.class.getClassLoader(),
					new Class<?>[] {BatchEngineTaskCompanyConfiguration.class},
					new InvocationHandler() {

						@Override
						public Object invoke(
							Object proxy, Method method, Object[] args) {

							String name = method.getName();

							if (name.equals("languageKeyResolutionEnabled")) {
								return enabled;
							}

							return null;
						}

					});

		return (ConfigurationProvider)ProxyUtil.newProxyInstance(
			ConfigurationProvider.class.getClassLoader(),
			new Class<?>[] {ConfigurationProvider.class},
			new InvocationHandler() {

				@Override
				public Object invoke(
					Object proxy, Method method, Object[] args) {

					String name = method.getName();

					if (name.equals("getCompanyConfiguration")) {
						return batchEngineTaskCompanyConfiguration;
					}

					return null;
				}

			});
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

						if (translation == null) {
							return key;
						}

						return translation;
					}

					return null;
				}

			});
	}

	private void _setConfigurationProvider(boolean enabled) throws Exception {
		_setField(
			"_configurationProvider", _createConfigurationProvider(enabled));
	}

	private void _setField(String name, Object value) throws Exception {
		Field field = LanguageKeyResolverImpl.class.getDeclaredField(name);

		field.setAccessible(true);

		field.set(_languageKeyResolverImpl, value);
	}

	private LanguageKeyResolverImpl _languageKeyResolverImpl;

}