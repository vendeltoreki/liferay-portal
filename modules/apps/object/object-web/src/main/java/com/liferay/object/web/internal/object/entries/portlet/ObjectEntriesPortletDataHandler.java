/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.object.entries.portlet;

import com.liferay.exportimport.kernel.lar.BasePortletDataHandler;
import com.liferay.exportimport.kernel.lar.DataLevel;
import com.liferay.exportimport.kernel.lar.ManifestSummary;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerBoolean;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.exportimport.kernel.lar.StagedModelType;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectViewLocalService;
import com.liferay.object.web.internal.object.entries.frontend.data.set.filter.factory.ObjectFieldFDSFilterFactoryRegistry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.xml.Element;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletPreferences;
import java.util.List;

/**
 * @author Vendel Toreki
 */
public class ObjectEntriesPortletDataHandler extends BasePortletDataHandler {

	public ObjectEntriesPortletDataHandler(
		ObjectActionLocalService objectActionLocalService,
		long objectDefinitionId,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectFieldFDSFilterFactoryRegistry objectFieldFDSFilterFactoryRegistry,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectScopeProviderRegistry objectScopeProviderRegistry,
		ObjectViewLocalService objectViewLocalService, Portal portal,
		PortletResourcePermission portletResourcePermission) {

		_objectActionLocalService = objectActionLocalService;
		_objectDefinitionId = objectDefinitionId;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectFieldFDSFilterFactoryRegistry =
			objectFieldFDSFilterFactoryRegistry;
		_objectFieldLocalService = objectFieldLocalService;
		_objectScopeProviderRegistry = objectScopeProviderRegistry;
		_objectViewLocalService = objectViewLocalService;
		_portal = portal;
		_portletResourcePermission = portletResourcePermission;

		activate();
	}

	public static final String NAMESPACE = "objects";

	public static final String SCHEMA_VERSION = "4.0.0";

	@Override
	public String getSchemaVersion() {
		return SCHEMA_VERSION;
	}

	protected void activate() {
		setDataLevel(DataLevel.SITE);

		try {
			ObjectDefinition objectDefinition = _objectDefinitionLocalService.getObjectDefinition(_objectDefinitionId);
			_objectClassName = objectDefinition.getClassName();
		}
		catch (PortalException e) {
			throw new RuntimeException(e);
		}


		//setDataLevel(DataLevel.PORTAL);

		/*setDeletionSystemEventStagedModelTypes(
			new StagedModelType(StagedExpandoTable.class),
			new StagedModelType(StagedExpandoColumn.class));
		setExportControls(
			new PortletDataHandlerBoolean(
				NAMESPACE, "expando-table", true, true, null,
				StagedExpandoTable.class.getName()),
			new PortletDataHandlerBoolean(
				NAMESPACE, "expando-column", true, true, null,
				StagedExpandoColumn.class.getName()));*/

		setExportControls(
			new PortletDataHandlerBoolean(
				NAMESPACE, "object-entry", true, true, null,
				_objectClassName)
			);
	}

	@Override
	protected PortletPreferences doDeleteData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		/*_stagedExpandoTableStagedModelRepository.deleteStagedModels(
			portletDataContext);*/

		return portletPreferences;
	}

	@Override
	protected String doExportData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		portletDataContext.addPortalPermissions();

		Element rootElement = addExportDataRootElement(portletDataContext);

		rootElement.addAttribute(
			"group-id", String.valueOf(portletDataContext.getScopeGroupId()));

		/*ExportActionableDynamicQuery actionableDynamicQuery =
			_stagedExpandoTableStagedModelRepository.
				getExportActionableDynamicQuery(portletDataContext);

		actionableDynamicQuery.performActions();

		actionableDynamicQuery =
			_stagedExpandoColumnStagedModelRepository.
				getExportActionableDynamicQuery(portletDataContext);

		actionableDynamicQuery.performActions();*/

		return getExportDataRootElementString(rootElement);
	}

	@Override
	protected PortletPreferences doImportData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences, String data)
		throws Exception {

		/*portletDataContext.importPortalPermissions();

		Element stagedExpandoTablesElement =
			portletDataContext.getImportDataGroupElement(
				StagedExpandoTable.class);

		List<Element> stagedExpandoTablesElements =
			stagedExpandoTablesElement.elements();

		for (Element stagedExpandoTableElement : stagedExpandoTablesElements) {
			StagedModelDataHandlerUtil.importStagedModel(
				portletDataContext, stagedExpandoTableElement);
		}

		Element stagedExpandoColumnsElement =
			portletDataContext.getImportDataGroupElement(
				StagedExpandoColumn.class);

		List<Element> stagedExpandoColumnsElements =
			stagedExpandoColumnsElement.elements();

		for (Element stagedExpandoColumnElement :
				stagedExpandoColumnsElements) {

			StagedModelDataHandlerUtil.importStagedModel(
				portletDataContext, stagedExpandoColumnElement);
		}*/

		return null;
	}

	@Override
	public String[] getClassNames() {
		return new String[] {_objectClassName};
	}

	@Override
	protected void doPrepareManifestSummary(
			PortletDataContext portletDataContext,
			PortletPreferences portletPreferences)
		throws Exception {

		ManifestSummary manifestSummary = portletDataContext.getManifestSummary();

		manifestSummary.addModelAdditionCount(
			_objectClassName, 10);

		/*ActionableDynamicQuery exportActionableDynamicQuery =
			_stagedExpandoTableStagedModelRepository.
				getExportActionableDynamicQuery(portletDataContext);

		exportActionableDynamicQuery.performCount();

		exportActionableDynamicQuery =
			_stagedExpandoColumnStagedModelRepository.
				getExportActionableDynamicQuery(portletDataContext);

		exportActionableDynamicQuery.performCount();*/
	}

	private String _objectClassName;
	private final ObjectActionLocalService _objectActionLocalService;
	private final long _objectDefinitionId;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectFieldFDSFilterFactoryRegistry
		_objectFieldFDSFilterFactoryRegistry;
	private final ObjectFieldLocalService _objectFieldLocalService;
	private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
	private final ObjectViewLocalService _objectViewLocalService;
	private final Portal _portal;
	private final PortletResourcePermission _portletResourcePermission;
}