/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.content.processor;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Vendel Töreki
 */
public class LanguageKeyBatchEngineContentProcessorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(_processor, "_language", _language);
	}

	@Test
	public void testProcessLeavesPlaceholderForUnknownKey() {
		Mockito.when(
			_language.get(
				Mockito.any(Locale.class), Mockito.anyString(),
				Mockito.isNull())
		).thenReturn(
			null
		);

		String content = "$LANG_KEY[unknown-key][en_US]";

		Assert.assertEquals(content, _processor.process(content));
	}

	@Test
	public void testProcessLeavesPlaceholderForUnresolvedLocale() {
		Mockito.when(
			_language.get(
				Mockito.any(Locale.class), Mockito.anyString(),
				Mockito.isNull())
		).thenReturn(
			null
		);

		String content = "$LANG_KEY[welcome][zz_ZZ]";

		Assert.assertEquals(content, _processor.process(content));
	}

	@Test
	public void testProcessLeavesUnrelatedContentUntouched() {
		String content = "Welcome to Liferay";

		Assert.assertSame(content, _processor.process(content));
	}

	@Test
	public void testProcessNullReturnsNull() {
		Assert.assertNull(_processor.process(null));
	}

	@Test
	public void testProcessResolvesEmbeddedPlaceholder() {
		Mockito.when(
			_language.get(LocaleUtil.US, "site-name", null)
		).thenReturn(
			"My Site"
		);

		Assert.assertEquals(
			"Hello My Site!",
			_processor.process("Hello $LANG_KEY[site-name][en_US]!"));
	}

	@Test
	public void testProcessResolvesMultiplePlaceholders() {
		Mockito.when(
			_language.get(LocaleUtil.US, "hello", null)
		).thenReturn(
			"Hello"
		);

		Mockito.when(
			_language.get(LocaleUtil.US, "world", null)
		).thenReturn(
			"World"
		);

		Assert.assertEquals(
			"Hello World",
			_processor.process(
				"$LANG_KEY[hello][en_US] $LANG_KEY[world][en_US]"));
	}

	@Test
	public void testProcessResolvesPlaceholder() {
		Mockito.when(
			_language.get(LocaleUtil.US, "welcome", null)
		).thenReturn(
			"Welcome"
		);

		Assert.assertEquals(
			"Welcome", _processor.process("$LANG_KEY[welcome][en_US]"));
	}

	private final Language _language = Mockito.mock(Language.class);
	private final LanguageKeyBatchEngineContentProcessor _processor =
		new LanguageKeyBatchEngineContentProcessor();

}