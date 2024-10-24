/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.batch.engine.action;

import com.liferay.batch.engine.action.ItemReaderPostAction;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.headless.delivery.dto.v1_0.Creator;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Carlos Correa
 */
@Component(service = ItemReaderPostAction.class)
public class AuditInfoItemReaderPostAction implements ItemReaderPostAction {

	@Override
	public void run(
			BatchEngineImportTask batchEngineImportTask,
			Map<String, Serializable> extendedProperties, Object item)
		throws ReflectiveOperationException {

		if (!GetterUtil.getBoolean(
				batchEngineImportTask.getParameters(
				).get(
					"keepOriginalUserIds"
				)) ||
			!(item instanceof ObjectEntry)) {

			return;
		}

		ObjectEntry objectEntry = (ObjectEntry)item;

		if (objectEntry.getProperties() == null) {
			return;
		}

		Map<String, Object> properties = objectEntry.getProperties();

		if (!properties.containsKey("creatorAuditInfo")) {
			return;
		}

		Map<String, Object> creatorInfo = (Map<String, Object>)properties.get(
			"creatorAuditInfo");

		if (objectEntry.getCreator() == null) {
			objectEntry.setCreator(() -> new Creator());
		}

		Creator creator = objectEntry.getCreator();

		String creatorErc = MapUtil.getString(
			creatorInfo, "externalReferenceCode");

		if (Validator.isNotNull(creatorErc)) {
			creator.setExternalReferenceCode(() -> creatorErc);
		}

		long creatorId = MapUtil.getLong(creatorInfo, "id");

		if (creatorId != 0) {
			creator.setId(() -> creatorId);
		}
	}

}