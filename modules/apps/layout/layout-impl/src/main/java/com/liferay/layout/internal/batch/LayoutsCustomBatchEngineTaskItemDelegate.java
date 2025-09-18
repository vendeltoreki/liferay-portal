/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.batch;

import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegateRegistry;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.headless.admin.site.dto.v1_0.SitePage;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.MapUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
 */
@Component(
	property = "batch.engine.task.item.delegate.name=LayoutsCustomBatchEngineTaskItemDelegate",
	service = BatchEngineTaskItemDelegate.class
)
public class LayoutsCustomBatchEngineTaskItemDelegate
	extends BaseBatchEngineTaskItemDelegate<SitePage> {

	@Override
	public Page<SitePage> read(
			Filter filter, Pagination pagination, Sort[] sorts,
			Map<String, Serializable> parameters, String search)
		throws Exception {

		String ercFilter = MapUtil.getString(parameters, "ercFilter");

		BatchEngineTaskItemDelegate<SitePage> batchEngineTaskItemDelegate =
			(BatchEngineTaskItemDelegate<SitePage>)
				_batchEngineTaskItemDelegateRegistry.
					getBatchEngineTaskItemDelegate(
						CompanyThreadLocal.getCompanyId(),
						SitePage.class.getName(), null);

		batchEngineTaskItemDelegate.setContextCompany(contextCompany);
		batchEngineTaskItemDelegate.setContextUser(contextUser);
		batchEngineTaskItemDelegate.setLanguageId(languageId);

		Page<SitePage> page = batchEngineTaskItemDelegate.read(
			filter, pagination, sorts, parameters, search);

		List<SitePage> sitePages = new ArrayList<>();

		for (SitePage sitePage : page.getItems()) {
			if (ercFilter.contains(sitePage.getExternalReferenceCode())) {
				sitePages.add(sitePage);
			}
		}

		return Page.of(sitePages);
	}

	@Reference
	private BatchEngineTaskItemDelegateRegistry
		_batchEngineTaskItemDelegateRegistry;

}