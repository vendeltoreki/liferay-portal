/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.page.template.internal.upgrade.v3_4_2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ConcurrentHashMapBuilder;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.MapUtil;

/**
 * @author Vendel Toreki
 */
public class UpgradeLayoutPageTemplateEntryResourcePermission extends UpgradeProcess {

	public UpgradeLayoutPageTemplateEntryResourcePermission(
		ResourceActionLocalService resourceActionLocalService, ResourcePermissionLocalService resourcePermissionLocalService) {

		_resourceActionLocalService = resourceActionLocalService;
		_resourcePermissionLocalService = resourcePermissionLocalService; 
	}
	
	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			copyResourcePermissions(_CLASS_NAME_LAYOUT_PROTOTYPE_PORTLET, _CLASS_NAME_LAYOUT_PAGE_TEMPLATE_PORTLET, true);
			copyResourcePermissions(LayoutPrototype.class.getName(), LayoutPageTemplateEntry.class.getName(), false);
		}		
	}

	
	protected void copyResourcePermissions(
		final String oldName, final String newName, boolean mapActions)
	throws PortalException {
		if (_log.isDebugEnabled()) {
			_log.debug("Copy resource permissions from "+oldName+" to "+newName);
		}
		
		ActionableDynamicQuery actionableDynamicQuery =
			_resourcePermissionLocalService.getActionableDynamicQuery();
	
		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property nameProperty = PropertyFactoryUtil.forName("name");
	
				dynamicQuery.add(nameProperty.eq(oldName));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(ResourcePermission resourcePermission) -> {
				resourcePermission.setName(newName);

				if (Objects.equals(
						resourcePermission.getPrimKey(), oldName)) {

					resourcePermission.setPrimKey(newName);
				}

				if (mapActions) {
					resourcePermission.setActionIds(
						getNewActionIds(
							oldName, newName, 0,
							resourcePermission.getActionIds()));
				}
				
				resourcePermission.setResourcePermissionId(
					increment());

				_resourcePermissionLocalService.addResourcePermission(
					resourcePermission);				
			});
	
		actionableDynamicQuery.performActions();
	}

	protected long getNewActionIds(
		String oldName, String newName, long currentActionIds,
		long oldActionIds) {

		Set<String> actionsIdsList = new HashSet<>();

		collectNewActionIds(
			actionsIdsList,
			_resourceActionLocalService.getResourceActions(oldName),
			oldActionIds);

		List<ResourceAction> newResourceActions =
			_resourceActionLocalService.getResourceActions(newName);

		collectNewActionIds(
			actionsIdsList, newResourceActions, currentActionIds);

		Stream<ResourceAction> resourceActionStream =
			newResourceActions.stream();

		Map<String, Long> map = resourceActionStream.collect(
			Collectors.toMap(
				resourceAction -> resourceAction.getActionId(),
				resourceAction -> resourceAction.getBitwiseValue()));

		Stream<String> actionsIdsStream = actionsIdsList.stream();

		return actionsIdsStream.mapToLong(
			actionId -> MapUtil.getLong(map, actionId)
		).sum();
	}
	
	protected void collectNewActionIds(
		Set<String> actionsIdsSet, List<ResourceAction> resourceActionList,
		long oldActionIds) {

		for (ResourceAction resourceAction : resourceActionList) {
			long bitwiseValue = resourceAction.getBitwiseValue();

			if ((oldActionIds & bitwiseValue) == bitwiseValue) {
				actionsIdsSet.add(
					MapUtil.getString(
						_resourceActionIdsMap, resourceAction.getActionId()));
			}
		}
	}
	
	private final ResourcePermissionLocalService _resourcePermissionLocalService;
	
	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeLayoutPageTemplateEntryResourcePermission.class);

	private final ResourceActionLocalService _resourceActionLocalService;
	
	private static final String _CLASS_NAME_LAYOUT_PROTOTYPE_PORTLET =
		"com_liferay_layout_prototype_web_portlet_LayoutPrototypePortlet";

	private static final String _CLASS_NAME_LAYOUT_PAGE_TEMPLATE_PORTLET =
		"com_liferay_layout_page_template_admin_web_portlet_LayoutPageTemplatesPortlet";
	
	private static final Map<String, String> _resourceActionIdsMap =
		ConcurrentHashMapBuilder.put(
			"ACCESS_IN_CONTROL_PANEL", "ADD_TO_PAGE"
		).put(
			"CONFIGURATION", "CONFIGURATION"
		).put(
			"DELETE", "DELETE"
		).put(
			"PERMISSIONS", "PERMISSIONS"
		).put(
			"PREFERENCES", "PREFERENCES"
		).put(
			"VIEW", "VIEW"
		).build();	
}