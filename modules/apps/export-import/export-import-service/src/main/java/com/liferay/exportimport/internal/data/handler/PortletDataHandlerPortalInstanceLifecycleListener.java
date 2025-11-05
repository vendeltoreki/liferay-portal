/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.data.handler;

import com.liferay.exportimport.data.handler.BatchEnginePortletDataHandlerRegistrar;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Company;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
 */
@Component(service = PortalInstanceLifecycleListener.class)
public class PortletDataHandlerPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) {
		if (FeatureFlagManagerUtil.isEnabled(
				company.getCompanyId(), "LPD-35914")) {

			_batchEnginePortletDataHandlerRegistrar.registerCompany(
				company.getCompanyId());
		}
	}

	@Override
	public void portalInstanceUnregistered(Company company) {
		_batchEnginePortletDataHandlerRegistrar.unregisterCompany(
			company.getCompanyId());
	}

	@Reference
	private BatchEnginePortletDataHandlerRegistrar
		_batchEnginePortletDataHandlerRegistrar;

}