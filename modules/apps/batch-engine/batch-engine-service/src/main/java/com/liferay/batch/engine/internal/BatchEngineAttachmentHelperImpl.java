/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal;

import com.liferay.batch.engine.BatchEngineAttachmentHelper;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.PortletPreferences;
import org.osgi.service.component.annotations.Component;


import java.util.Map;

/**
 * @author Vendel Toreki
 */
@Component(service = BatchEngineAttachmentHelper.class)
public class BatchEngineAttachmentHelperImpl
	implements BatchEngineAttachmentHelper {

	@Override
	public void exportAttachments(
		String portletId,
		PortletDataContext portletDataContext,
		PortletPreferences portletPreferences)
		throws Exception {

		_log.fatal("Exporting attachments: "+portletId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineAttachmentHelperImpl.class);

}