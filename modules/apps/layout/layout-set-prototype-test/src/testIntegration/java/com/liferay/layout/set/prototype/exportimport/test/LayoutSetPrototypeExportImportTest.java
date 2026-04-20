/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.set.prototype.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationSettingsMapFactoryUtil;
import com.liferay.exportimport.kernel.configuration.constants.ExportImportConfigurationConstants;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalServiceUtil;
import com.liferay.exportimport.kernel.service.ExportImportLocalServiceUtil;
import com.liferay.exportimport.test.util.lar.BasePortletExportImportTestCase;
import com.liferay.layout.set.prototype.constants.LayoutSetPrototypePortletKeys;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.Serializable;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Eduardo García
 */
@RunWith(Arquillian.class)
public class LayoutSetPrototypeExportImportTest
	extends BasePortletExportImportTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Override
	public String getNamespace() {
		return "layout_set_prototypes";
	}

	@Override
	public String getPortletId() {
		return LayoutSetPrototypePortletKeys.LAYOUT_SET_PROTOTYPE;
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		UserTestUtil.setUser(TestPropsValues.getUser());
	}

	@Override
	@Test
	public void testExportImportAssetLinks() throws Exception {
	}

	@Test
	public void testExportImportLayoutSetPrototype() throws Exception {
		exportImportLayoutSetPrototype(false);
	}

	@Test
	public void testExportImportLayoutSetPrototypeInTransaction()
		throws Exception {

		LayoutSetPrototypeLocalServiceUtil.deleteLayoutSetPrototypes();

		LayoutSetPrototype exportedLayoutSetPrototype =
			LayoutTestUtil.addLayoutSetPrototype(RandomTestUtil.randomString());

		Group exportedLayoutSetPrototypeGroup =
			exportedLayoutSetPrototype.getGroup();

		LayoutTestUtil.addTypePortletLayout(
			exportedLayoutSetPrototypeGroup, true);

		int privateLayoutsPageCount =
			exportedLayoutSetPrototypeGroup.getPrivateLayoutsPageCount();

		_exportImportLayoutSetPrototypePortlet();

		LayoutSetPrototype importedLayoutSetPrototype =
			LayoutSetPrototypeLocalServiceUtil.
				getLayoutSetPrototypeByUuidAndCompanyId(
					exportedLayoutSetPrototype.getUuid(),
					exportedLayoutSetPrototype.getCompanyId());

		Group importedLayoutSetPrototypeGroup =
			importedLayoutSetPrototype.getGroup();

		Assert.assertEquals(
			privateLayoutsPageCount,
			importedLayoutSetPrototypeGroup.getPrivateLayoutsPageCount());
	}

	@Test
	public void testExportImportLayoutSetPrototypeWithLayoutPrototype()
		throws Exception {

		exportImportLayoutSetPrototype(true);
	}

	protected void exportImportLayoutSetPrototype(boolean layoutPrototype)
		throws Exception {

		// Exclude default site templates

		LayoutSetPrototypeLocalServiceUtil.deleteLayoutSetPrototypes();

		LayoutSetPrototype exportedLayoutSetPrototype =
			LayoutTestUtil.addLayoutSetPrototype(RandomTestUtil.randomString());

		Group exportedLayoutSetPrototypeGroup =
			exportedLayoutSetPrototype.getGroup();

		if (layoutPrototype) {
			LayoutPrototype exportedLayoutPrototype =
				LayoutTestUtil.addLayoutPrototype(
					RandomTestUtil.randomString());

			LayoutTestUtil.addTypePortletLayout(
				exportedLayoutSetPrototypeGroup, true, exportedLayoutPrototype,
				true);
		}
		else {
			LayoutTestUtil.addTypePortletLayout(
				exportedLayoutSetPrototypeGroup, true);
		}

		exportImportPortlet(LayoutSetPrototypePortletKeys.LAYOUT_SET_PROTOTYPE);

		LayoutSetPrototype importedLayoutSetPrototype =
			LayoutSetPrototypeLocalServiceUtil.
				getLayoutSetPrototypeByUuidAndCompanyId(
					exportedLayoutSetPrototype.getUuid(),
					exportedLayoutSetPrototype.getCompanyId());

		Group importedLayoutSetPrototypeGroup =
			importedLayoutSetPrototype.getGroup();

		Assert.assertEquals(
			exportedLayoutSetPrototypeGroup.getPrivateLayoutsPageCount(),
			importedLayoutSetPrototypeGroup.getPrivateLayoutsPageCount());

		LayoutSetPrototypeLocalServiceUtil.deleteLayoutSetPrototype(
			exportedLayoutSetPrototype);
	}

	@Override
	protected Map<String, String[]> getExportParameterMap() throws Exception {
		Map<String, String[]> parameterMap = super.getExportParameterMap();

		addParameter(parameterMap, "page-templates", true);

		return parameterMap;
	}

	@Override
	protected Map<String, String[]> getImportParameterMap() throws Exception {
		Map<String, String[]> parameterMap = super.getExportParameterMap();

		addParameter(parameterMap, "page-templates", true);

		return parameterMap;
	}

	@Override
	protected void testExportImportDisplayStyle(long groupId, String scopeType)
		throws Exception {
	}

	private void _executeLayoutSetPrototypeImportInTransaction(
		ExportImportConfiguration exportImportConfiguration) {

		try {
			TransactionInvokerUtil.invoke(
				_transactionConfig,
				() -> {
					ExportImportLocalServiceUtil.importPortletInfo(
						exportImportConfiguration, larFile);

					return null;
				});
		}
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	private void _exportImportLayoutSetPrototypePortlet() throws Exception {
		User user = TestPropsValues.getUser();

		Map<String, Serializable> settingsMap =
			ExportImportConfigurationSettingsMapFactoryUtil.
				buildExportPortletSettingsMap(
					user, layout.getPlid(), layout.getGroupId(),
					LayoutSetPrototypePortletKeys.LAYOUT_SET_PROTOTYPE,
					getExportParameterMap(), StringPool.BLANK);

		ExportImportConfiguration exportImportConfiguration =
			ExportImportConfigurationLocalServiceUtil.
				addDraftExportImportConfiguration(
					user.getUserId(),
					ExportImportConfigurationConstants.
						TYPE_PUBLISH_PORTLET_LOCAL,
					settingsMap);

		larFile = ExportImportLocalServiceUtil.exportPortletInfoAsFile(
			exportImportConfiguration);

		importedLayout = LayoutTestUtil.addTypePortletLayout(importedGroup);

		settingsMap =
			ExportImportConfigurationSettingsMapFactoryUtil.
				buildImportPortletSettingsMap(
					user, importedLayout.getPlid(), importedGroup.getGroupId(),
					LayoutSetPrototypePortletKeys.LAYOUT_SET_PROTOTYPE,
					getImportParameterMap());

		exportImportConfiguration =
			ExportImportConfigurationLocalServiceUtil.
				updateExportImportConfiguration(
					user.getUserId(),
					exportImportConfiguration.getExportImportConfigurationId(),
					StringPool.BLANK, StringPool.BLANK, settingsMap,
					new ServiceContext());

		exportImportConfiguration.setGroupId(importedGroup.getGroupId());

		exportImportConfiguration =
			ExportImportConfigurationLocalServiceUtil.
				updateExportImportConfiguration(exportImportConfiguration);

		LayoutSetPrototypeLocalServiceUtil.deleteLayoutSetPrototypes();

		_executeLayoutSetPrototypeImportInTransaction(
			exportImportConfiguration);
	}

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRES_NEW, new Class<?>[] {Exception.class});

}