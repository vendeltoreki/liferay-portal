/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.batch.engine;

import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegateRegistry;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Vendel Toreki
 */
@Component(
	property = "batch.engine.task.item.delegate.name=BulkTaxonomyCategoryAdd",
	service = BatchEngineTaskItemDelegate.class
)
public class TaxonomyCategoryBatchEngineTaskItemDelegate extends
	BaseBatchEngineTaskItemDelegate<ObjectEntry> {

	@Override
	public EntityModel getEntityModel(Map<String, List<String>> multivaluedMap)
		throws Exception {

		return null;
	}

	@Override
	public Class<ObjectEntry> getItemClass() {
		return ObjectEntry.class;
	}

	@Override
	public Page<ObjectEntry> read(
		Filter filter, Pagination pagination,
		Sort[] sorts,
		Map<String, Serializable> parameters,
		String search) throws Exception {

		return null;
	}

	@Override
	public boolean hasUpdateStrategy(String updateStrategy) {
		return updateStrategy.equals("UPDATE") || updateStrategy.equals("PARTIAL_UPDATE");
	}

	@Override
	public void update(
		Collection<ObjectEntry> items, Map<String, Serializable> parameters)
		throws Exception {

		BatchEngineTaskItemDelegate<ObjectEntry> batchEngineTaskItemDelegate =
			(BatchEngineTaskItemDelegate<ObjectEntry>)_batchEngineTaskItemDelegateRegistry.getBatchEngineTaskItemDelegate(
				contextCompany.getCompanyId(),
				ObjectEntry.class.getName(),
				"Blog");


		for (ObjectEntry item : items) {
			/*batchEngineTaskItemDelegate.read(new Filter(), )

			com.liferay.object.model.ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(item.getId());

			_objectEntryManager.getObjectEntry(
				contextCompany.getCompanyId(),
				new DefaultDTOConverterContext( ),
				objectEntry.getExternalReferenceCode(),
				objectEntry.getObjectDefinitionId(),
				"");*/

		}

		batchEngineTaskItemDelegate.update(items, parameters);

	}

	@Reference
	private BatchEngineTaskItemDelegateRegistry
		_batchEngineTaskItemDelegateRegistry;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectEntryManager _objectEntryManager;

}
