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

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;

import java.util.Objects;

/**
 * @author Vendel Toreki
 */
public class UpgradeLayoutPageTemplateEntryResourcePermission
	extends UpgradeProcess {

	public UpgradeLayoutPageTemplateEntryResourcePermission(
		ResourcePermissionLocalService resourcePermissionLocalService) {

		_resourcePermissionLocalService = resourcePermissionLocalService;
	}

	protected void copyResourcePermissions(
			final String oldName, final String newName)
		throws PortalException {

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Copy resource permissions from ", oldName, " to ",
					newName));
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

				if (Objects.equals(resourcePermission.getPrimKey(), oldName)) {
					resourcePermission.setPrimKey(newName);
				}

				ResourcePermission existingResourcePermission =
					_resourcePermissionLocalService.fetchResourcePermission(
						resourcePermission.getCompanyId(),
						resourcePermission.getName(),
						resourcePermission.getScope(),
						resourcePermission.getPrimKey(),
						resourcePermission.getRoleId());

				if (existingResourcePermission == null) {
					resourcePermission.setResourcePermissionId(increment());

					_resourcePermissionLocalService.addResourcePermission(
						resourcePermission);
				}
			});

		actionableDynamicQuery.performActions();
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			copyResourcePermissions(
				LayoutPrototype.class.getName(),
				LayoutPageTemplateEntry.class.getName());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeLayoutPageTemplateEntryResourcePermission.class);

	private final ResourcePermissionLocalService
		_resourcePermissionLocalService;

}