/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.content.processor;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Vendel Töreki
 */
public class LanguageKeyMapExpanderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(_expander, "_language", _language);

		Set<Locale> locales = new HashSet<>(
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN));

		Mockito.when(
			_language.getAvailableLocales()
		).thenReturn(
			locales
		);
	}

	@Test
	public void testExpandHandlesNullMap() {
		_expander.expand(null);
	}

	@Test
	public void testExpandLeavesLiteralStringUntouched() {
		Map<String, Object> map = HashMapBuilder.<String, Object>put(
			"name_i18n", "Just a string"
		).build();

		_expander.expand(map);

		Assert.assertEquals("Just a string", map.get("name_i18n"));
	}

	@Test
	public void testExpandLeavesUnresolvedPlaceholderAlone() {
		Mockito.when(
			_language.get(
				Mockito.any(Locale.class), Mockito.anyString(),
				Mockito.isNull())
		).thenReturn(
			null
		);

		Map<String, Object> map = HashMapBuilder.<String, Object>put(
			"name_i18n", "$LFR_LANGUAGE_KEY-unknown$"
		).build();

		_expander.expand(map);

		Assert.assertEquals("$LFR_LANGUAGE_KEY-unknown$", map.get("name_i18n"));
	}

	@Test
	public void testExpandRecursesIntoListsOfMaps() {
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

		Map<String, Object> child = HashMapBuilder.<String, Object>put(
			"title_i18n", "$LFR_LANGUAGE_KEY-welcome$"
		).build();

		_expander.expand(
			HashMapBuilder.<String, Object>put(
				"children", (Object)List.of(child)
			).build());

		Assert.assertTrue(child.get("title_i18n") instanceof Map);
	}

	@Test
	public void testExpandRecursesIntoNestedMaps() {
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

		Map<String, Object> nested = HashMapBuilder.<String, Object>put(
			"title_i18n", "$LFR_LANGUAGE_KEY-welcome$"
		).build();

		_expander.expand(
			HashMapBuilder.<String, Object>put(
				"nested", (Object)nested
			).build());

		Assert.assertTrue(nested.get("title_i18n") instanceof Map);
	}

	@Test
	public void testExpandSkipsKeyWithoutI18nSuffix() {
		Map<String, Object> map = HashMapBuilder.<String, Object>put(
			"description", "$LFR_LANGUAGE_KEY-welcome$"
		).build();

		_expander.expand(map);

		Assert.assertEquals(
			"$LFR_LANGUAGE_KEY-welcome$", map.get("description"));
	}

	@Test
	public void testExpandWholeMapPlaceholder() {
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

		Map<String, Object> map = HashMapBuilder.<String, Object>put(
			"name_i18n", "$LFR_LANGUAGE_KEY-welcome$"
		).build();

		_expander.expand(map);

		Object resolved = map.get("name_i18n");

		Assert.assertTrue(
			"Expected Map but got " + resolved, resolved instanceof Map);

		Map<String, String> resolvedMap = (Map<String, String>)resolved;

		Assert.assertEquals(resolvedMap.toString(), 2, resolvedMap.size());
		Assert.assertEquals("Welcome", resolvedMap.get("en_US"));
		Assert.assertEquals("Bienvenido", resolvedMap.get("es_ES"));
	}

	private final LanguageKeyMapExpander _expander =
		new LanguageKeyMapExpander();
	private final Language _language = Mockito.mock(Language.class);

}