/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.internal.resource.v1_0;

import com.liferay.exportimport.kernel.lar.ExportImportDateUtil;
import com.liferay.exportimport.kernel.lar.ExportImportHelper;
import com.liferay.exportimport.kernel.lar.ManifestSummary;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataContextFactoryUtil;
import com.liferay.exportimport.kernel.lar.PortletDataException;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.rest.dto.v1_0.PortletEntry;
import com.liferay.exportimport.rest.resource.v1_0.PortletSectionResource;
import com.liferay.exportimport.rest.dto.v1_0.PortletSection;


import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.vulcan.pagination.Page;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/portlet-section.properties",
	scope = ServiceScope.PROTOTYPE, service = PortletSectionResource.class
)
public class PortletSectionResourceImpl extends BasePortletSectionResourceImpl {
	@Override
	public com.liferay.portal.vulcan.pagination.Page<PortletSection>
			getSiteExportInfoSectionsPage(
				Long siteId,
				com.liferay.portal.kernel.search.filter.Filter filter)
		throws Exception {

		List<Portlet> exportablePortlets =
			_exportImportHelper.getExportablePortlets(
				contextCompany.getCompanyId(), false, siteId);

		Map<String, List<Portlet>> portletsBySectionNames = new HashMap<>();

		for (Portlet portlet : exportablePortlets) {
			PortletDataHandler portletDataHandler =
				portlet.getPortletDataHandlerInstance();

			String sectionName = portletDataHandler.getSectionName();

			if (sectionName == null) {
				continue;
			}

			if (portletsBySectionNames.containsKey(sectionName)) {
				List<Portlet> portlets = portletsBySectionNames.get(sectionName);
				portlets.add(portlet);
			}
			else {
				List<Portlet> portlets = new ArrayList<>();
				portlets.add(portlet);
				portletsBySectionNames.put(sectionName, portlets);
			}
		}

		List<PortletSection> portletSections = new ArrayList<>();

		for (String sectionName : portletsBySectionNames.keySet()) {
			List<Portlet> portlets = portletsBySectionNames.get(sectionName);

			PortletSection portletSection = new PortletSection();
			portletSection.setName(sectionName);

			List<PortletEntry> portletEntries = new ArrayList<>();

			for (Portlet portlet : portlets) {
				PortletDataHandler portletDataHandler =
					portlet.getPortletDataHandlerInstance();

				PortletEntry portletEntry = new PortletEntry();
				portletEntry.setPortletId(portlet.getPortletId());

				updateCounts(siteId, portletDataHandler, portletEntry);

				portletEntries.add(portletEntry);
			}

			portletSection.setPortletEntries(
				portletEntries.toArray(new PortletEntry[0]));

			portletSections.add(portletSection);
		}

		return Page.of(portletSections);
	}

	private void updateCounts(
		Long siteId, PortletDataHandler portletDataHandler,
		PortletEntry portletEntry) throws PortletDataException {
		PortletDataContext portletDataContext = PortletDataContextFactoryUtil.createPreparePortletDataContext(contextCompany.getCompanyId(),
			siteId, ExportImportDateUtil.RANGE_ALL, null, null);

		portletDataHandler.prepareManifestSummary(portletDataContext);

		ManifestSummary manifestSummary = portletDataContext.getManifestSummary();

		long exportModelCount = portletDataHandler.getExportModelCount(manifestSummary);

		long modelDeletionCount = manifestSummary.getModelDeletionCount(
			portletDataHandler.getDeletionSystemEventStagedModelTypes());

		portletEntry.setCount(exportModelCount);
	}


	@Reference
	private ExportImportHelper _exportImportHelper;
}