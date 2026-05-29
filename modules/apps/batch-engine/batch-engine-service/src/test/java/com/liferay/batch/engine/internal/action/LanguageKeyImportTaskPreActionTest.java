/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.context.ImportTaskContext;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Vendel Töreki
 */
public class LanguageKeyImportTaskPreActionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_languageKeyImportTaskPreAction, "_language", _language);

		Mockito.when(
			_batchEngineImportTask.getBatchEngineImportTaskId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);
	}

	@Test
	public void testRunIgnoresNullItem() throws Exception {
		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, null);
	}

	@Test
	public void testRunLeavesEmptyMapAlone() throws Exception {
		TestItem testItem = new TestItem();

		testItem.setName_i18n(new HashMap<>());

		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, testItem);

		Assert.assertTrue(
			testItem.getName_i18n(
			).isEmpty());
	}

	@Test
	public void testRunLeavesUnresolvedPlaceholderUntouched() throws Exception {
		Mockito.when(
			_language.get(
				Mockito.any(Locale.class), Mockito.anyString(),
				Mockito.isNull())
		).thenReturn(
			null
		);

		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "$LANG_KEY[unknown][en_US]"
			).build());

		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, testItem);

		Assert.assertEquals(
			"$LANG_KEY[unknown][en_US]",
			testItem.getName_i18n(
			).get(
				"en_US"
			));
	}

	@Test
	public void testRunLeavesValueWithoutPlaceholderAlone() throws Exception {
		TestItem testItem = new TestItem();

		Map<String, String> map = HashMapBuilder.put(
			"en_US", "Hello World"
		).build();

		testItem.setName_i18n(map);

		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, testItem);

		Assert.assertEquals("Hello World", map.get("en_US"));
	}

	@Test
	public void testRunResolvesEmbeddedPlaceholder() throws Exception {
		Mockito.when(
			_language.get(LocaleUtil.US, "site-name", null)
		).thenReturn(
			"My Site"
		);

		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "Welcome to $LANG_KEY[site-name][en_US]!"
			).build());

		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, testItem);

		Assert.assertEquals(
			"Welcome to My Site!",
			testItem.getName_i18n(
			).get(
				"en_US"
			));
	}

	@Test
	public void testRunResolvesPerLocaleAcrossLocales() throws Exception {
		Mockito.when(
			_language.get(LocaleUtil.US, "welcome", null)
		).thenReturn(
			"Welcome"
		);

		Mockito.when(
			_language.get(LocaleUtil.SPAIN, "welcome", null)
		).thenReturn(
			"Bienvenido"
		);

		TestItem testItem = new TestItem();

		testItem.setName_i18n(
			HashMapBuilder.put(
				"en_US", "$LANG_KEY[welcome][en_US]"
			).put(
				"es_ES", "$LANG_KEY[welcome][es_ES]"
			).build());

		_languageKeyImportTaskPreAction.run(
			_batchEngineImportTask, null, _importTaskContext, testItem);

		Map<String, String> resolvedMap = testItem.getName_i18n();

		Assert.assertEquals(
			resolvedMap.toString(), "Welcome", resolvedMap.get("en_US"));
		Assert.assertEquals(
			resolvedMap.toString(), "Bienvenido", resolvedMap.get("es_ES"));
	}

	private final BatchEngineImportTask _batchEngineImportTask = Mockito.mock(
		BatchEngineImportTask.class);
	private final ImportTaskContext _importTaskContext =
		new ImportTaskContext();
	private final Language _language = Mockito.mock(Language.class);
	private final LanguageKeyImportTaskPreAction
		_languageKeyImportTaskPreAction = new LanguageKeyImportTaskPreAction();

	private static class TestItem {

		public Map<String, String> getName_i18n() {
			return _name_i18n;
		}

		public void setName_i18n(Map<String, String> name_i18n) {
			_name_i18n = name_i18n;
		}

		private Map<String, String> _name_i18n;

	}

}