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

package com.liferay.exportimport.internal.messaging;

import java.util.Date;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.liferay.exportimport.configuration.ExportImportServiceConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.SchedulerEntryImpl;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.Trigger;
import com.liferay.portal.kernel.scheduler.TriggerFactory;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.SystemEventLocalService;
import com.liferay.portal.kernel.util.Time;

/**
 * @author Vendel Toreki
 */
@Component(
	configurationPid = "com.liferay.batch.engine.configuration.BatchEngineTaskConfiguration",
	immediate = true, service = MessageListener.class
)
public class DeletionSystemEventCleanerMessageListener extends BaseMessageListener {

	@Activate
	protected void activate(Map<String, Object> properties) {
		ExportImportServiceConfiguration batchEngineTaskConfiguration =
			ConfigurableUtil.createConfigurable(
				ExportImportServiceConfiguration.class, properties);

		String className =
			DeletionSystemEventCleanerMessageListener.class.getName();
		int scanInterval =
			batchEngineTaskConfiguration.deletionSystemEventCleanerScanInterval();

		Trigger trigger = _triggerFactory.createTrigger(
			className, className,
			new Date(System.currentTimeMillis() + (scanInterval * Time.DAY)),
			null, scanInterval, TimeUnit.DAY);

		_schedulerEngineHelper.register(
			this, new SchedulerEntryImpl(className, trigger),
			DestinationNames.SCHEDULER_DISPATCH);
	}

	@Deactivate
	protected void deactivate() {
		_schedulerEngineHelper.unregister(this);
	}

	@Override
	protected void doReceive(Message message) throws Exception {
		deleteInvalidSystemEvents();
	}

	protected void deleteInvalidSystemEvents() throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			_groupLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property liveGroupIdProperty = PropertyFactoryUtil.forName(
					"liveGroupId");
				Property remoteStagingGroupCountProperty =
					PropertyFactoryUtil.forName("remoteStagingGroupCount");

				dynamicQuery.add(
					RestrictionsFactoryUtil.or(
						liveGroupIdProperty.ne(0L),
						remoteStagingGroupCountProperty.gt(0)));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(Group group) -> {
				long liveGroupId = group.getLiveGroupId();

				if (liveGroupId == 0) {
					liveGroupId = group.getGroupId();
				}

				if (!_systemEventLocalService.validateGroup(liveGroupId)) {
					_systemEventLocalService.deleteSystemEvents(liveGroupId);
				}
			});

		actionableDynamicQuery.performActions();
	}	
	@Reference
	private GroupLocalService
		_groupLocalService;

	@Reference
	private SystemEventLocalService
		_systemEventLocalService;

	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

	@Reference
	private TriggerFactory _triggerFactory;

}