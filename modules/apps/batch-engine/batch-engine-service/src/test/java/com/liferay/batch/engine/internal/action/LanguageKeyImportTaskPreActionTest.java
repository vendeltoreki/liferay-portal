/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.model.BatchEngineImportTask;
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
public class LanguageKeyImportTaskPreActionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_languageKeyImportTaskPreAction = new LanguageKeyImportTaskPreAction();

		Field field = LanguageKeyImportTaskPreAction.class.getDeclaredField(
			"_language");

		field.setAccessible(true);

		field.set(_languageKeyImportTaskPreAction, _createLanguage());
	}

	@Test
	public void testEmbeddedPlaceholderResolvedInline() throws Exception {

		// AC4

		Assert.assertEquals(
			"prefix Welcome suffix",
			_resolve(
				"name_i18n", "en_US",
				"prefix $LANG_KEY[welcome][en_US] suffix"));
	}

	@Test
	public void testMalformedEmptyKeyLeftUnchanged() throws Exception {

		// AC5

		String value = "$LANG_KEY[][en_US]";

		Assert.assertEquals(value, _resolve("name_i18n", "en_US", value));
	}

	@Test
	public void testMalformedLocaleFormatLeftUnchanged() throws Exception {

		// AC5

		String value = "$LANG_KEY[welcome][en-US]";

		Assert.assertEquals(value, _resolve("name_i18n", "en_US", value));
	}

	@Test
	public void testMalformedMissingLocaleBracketLeftUnchanged()
		throws Exception {

		// AC5

		String value = "$LANG_KEY[welcome]";

		Assert.assertEquals(value, _resolve("name_i18n", "en_US", value));
	}

	@Test
	public void testMultipleLocalesResolvedIndependently() throws Exception {

		// AC1

		Map<String, String> resolvedI18nMap = _run(
			"name_i18n",
			LinkedHashMapBuilder.put(
				"en_US", "$LANG_KEY[welcome][en_US]"
			).put(
				"es_ES", "$LANG_KEY[welcome][es_ES]"
			).build());

		Assert.assertEquals("Welcome", resolvedI18nMap.get("en_US"));
		Assert.assertEquals("Bienvenido", resolvedI18nMap.get("es_ES"));
	}

	@Test
	public void testPlainStringFieldNotScanned() throws Exception {

		// AC8

		TestItem testItem = new TestItem();

		testItem.setDescription("$LANG_KEY[welcome][en_US]");

		_languageKeyImportTaskPreAction.run(
			_createBatchEngineImportTask(), null, null, testItem);

		Assert.assertEquals(
			"$LANG_KEY[welcome][en_US]", testItem.getDescription());
	}

	@Test
	public void testUnknownKeyLeftEmpty() throws Exception {

		// AC2

		Assert.assertEquals(
			"", _resolve("name_i18n", "en_US", "$LANG_KEY[missing][en_US]"));
	}

	@Test
	public void testUnknownLocaleCaseMismatchLeftEmpty() throws Exception {

		// AC2, AC6

		Assert.assertEquals(
			"", _resolve("name_i18n", "en_US", "$LANG_KEY[welcome][en_us]"));
	}

	@Test
	public void testValidPlaceholderResolved() throws Exception {

		// AC1

		Assert.assertEquals(
			"Welcome",
			_resolve("name_i18n", "en_US", "$LANG_KEY[welcome][en_US]"));
	}

	@Test
	public void testWhitespaceInBracketsLeftUnchanged() throws Exception {

		// AC7

		String value = "$LANG_KEY[ welcome ][ en_US ]";

		Assert.assertEquals(value, _resolve("name_i18n", "en_US", value));
	}

	public static class TestItem {

		public String getDescription() {
			return _description;
		}

		public Map<String, String> getName_i18n() {
			return _name_i18n;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public void setName_i18n(Map<String, String> name_i18n) {
			_name_i18n = name_i18n;
		}

		private String _description;
		private Map<String, String> _name_i18n;

	}

	private BatchEngineImportTask _createBatchEngineImportTask() {
		return (BatchEngineImportTask)ProxyUtil.newProxyInstance(
			BatchEngineImportTask.class.getClassLoader(),
			new Class<?>[] {BatchEngineImportTask.class},
			new InvocationHandler() {

				@Override
				public Object invoke(
					Object proxy, Method method, Object[] args) {

					String name = method.getName();

					if (name.equals("getCompanyId")) {
						return _COMPANY_ID;
					}

					if (name.equals("getBatchEngineImportTaskId")) {
						return 1L;
					}

					return null;
				}

			});
	}

	private Language _createLanguage() {
		Map<String, String> translations = HashMapBuilder.put(
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

					if (name.equals("getAvailableLocales")) {
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

	private String _resolve(String fieldName, String locale, String value)
		throws Exception {

		Map<String, String> resolvedI18nMap = _run(
			fieldName,
			LinkedHashMapBuilder.put(
				locale, value
			).build());

		return resolvedI18nMap.get(locale);
	}

	private Map<String, String> _run(
			String fieldName, Map<String, String> i18nMap)
		throws Exception {

		TestItem testItem = new TestItem();

		if (fieldName.equals("name_i18n")) {
			testItem.setName_i18n(i18nMap);
		}

		_languageKeyImportTaskPreAction.run(
			_createBatchEngineImportTask(), null, null, testItem);

		return testItem.getName_i18n();
	}

	private static final long _COMPANY_ID = 1L;

	private LanguageKeyImportTaskPreAction _languageKeyImportTaskPreAction;

}