/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.language.LanguageKeyResolver;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
			"_languageKeyResolver");

		field.setAccessible(true);

		field.set(
			_languageKeyImportTaskPreAction, new TestLanguageKeyResolver());
	}

	@Test
	public void testFullExpansionResolved() throws Exception {
		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "welcome"
			).build());

		_languageKeyImportTaskPreAction.run(null, null, null, testItem);

		Map<String, String> name_i18n = testItem.getName_i18n();

		Assert.assertEquals("Welcome", name_i18n.get("en_US"));
		Assert.assertEquals("Bienvenido", name_i18n.get("es_ES"));
	}

	@Test
	public void testI18nFieldResolved() throws Exception {

		// AC1, AC8

		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "$LANG_KEY[welcome][en_US]"
			).build());

		_languageKeyImportTaskPreAction.run(null, null, null, testItem);

		Map<String, String> name_i18n = testItem.getName_i18n();

		Assert.assertEquals("Welcome", name_i18n.get("en_US"));
	}

	@Test
	public void testI18nValueWithoutPlaceholderUnchanged() throws Exception {
		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "Hello"
			).build());

		_languageKeyImportTaskPreAction.run(null, null, null, testItem);

		Map<String, String> name_i18n = testItem.getName_i18n();

		Assert.assertEquals("Hello", name_i18n.get("en_US"));
	}

	@Test
	public void testLabelMapResolved() throws Exception {
		TestItem testItem = new TestItem();

		testItem.setLabel(
			HashMapBuilder.put(
				"en_US", "$LANG_KEY[welcome][en_US]"
			).build());

		_languageKeyImportTaskPreAction.run(null, null, null, testItem);

		Map<String, String> label = testItem.getLabel();

		Assert.assertEquals("Welcome", label.get("en_US"));
	}

	@Test
	public void testPlainStringFieldNotScanned() throws Exception {

		// AC8

		TestItem testItem = new TestItem();

		testItem.setDescription("$LANG_KEY[welcome][en_US]");

		_languageKeyImportTaskPreAction.run(null, null, null, testItem);

		Assert.assertEquals(
			"$LANG_KEY[welcome][en_US]", testItem.getDescription());
	}

	public static class TestItem {

		public String getDescription() {
			return _description;
		}

		public Map<String, String> getLabel() {
			return _label;
		}

		public Map<String, String> getName_i18n() {
			return _name_i18n;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public void setLabel(Map<String, String> label) {
			_label = label;
		}

		public void setName_i18n(Map<String, String> name_i18n) {
			_name_i18n = name_i18n;
		}

		private String _description;
		private Map<String, String> _label;
		private Map<String, String> _name_i18n;

	}

	private LanguageKeyImportTaskPreAction _languageKeyImportTaskPreAction;

	private static class TestLanguageKeyResolver
		implements LanguageKeyResolver {

		@Override
		public Map<Locale, String> resolve(Map<Locale, String> localizedMap) {
			if ((localizedMap.size() == 1) &&
				Objects.equals(localizedMap.get(LocaleUtil.US), "welcome")) {

				return LinkedHashMapBuilder.put(
					LocaleUtil.US, "Welcome"
				).put(
					LocaleUtil.SPAIN, "Bienvenido"
				).build();
			}

			Map<Locale, String> resolvedLocalizedMap = new LinkedHashMap<>();

			for (Map.Entry<Locale, String> entry : localizedMap.entrySet()) {
				resolvedLocalizedMap.put(
					entry.getKey(), resolve(entry.getValue()));
			}

			return resolvedLocalizedMap;
		}

		@Override
		public String resolve(String value) {
			if (value.equals("$LANG_KEY[welcome][en_US]")) {
				return "Welcome";
			}

			return value;
		}

	}

}