/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.staging.taglib.servlet.taglib;

import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.staging.taglib.internal.servlet.ServletContextUtil;
import com.liferay.taglib.util.IncludeTag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Péter Borkuti
 */
public class ProcessInfoTag extends IncludeTag {

	public ExportImportConfiguration getExportImportConfiguration() {
		return _exportImportConfiguration;
	}

	public void setExportImportConfiguration(ExportImportConfiguration exportImportConfiguration) {
		_exportImportConfiguration = exportImportConfiguration;
	}

	public BackgroundTask getBackgroundTask() {
		return _backgroundTask;
	}

	public void setBackgroundTask(BackgroundTask backgroundTask) {
		_backgroundTask = backgroundTask;
	}

	@Override
	public void setPageContext(PageContext pageContext) {
		super.setPageContext(pageContext);

		setServletContext(ServletContextUtil.getServletContext());
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_backgroundTask = null;
		_exportImportConfiguration = null;
	}

	@Override
	protected String getPage() {
		return _PAGE;
	}

	@Override
	protected void setAttributes(HttpServletRequest httpServletRequest) {
		httpServletRequest.setAttribute(
			"liferay-staging:process-info:backgroundTask", _backgroundTask);

		if (_exportImportConfiguration != null) {
			StringBundler stringBundler = new StringBundler();

			Map<String, Serializable> settingsMap =
				_exportImportConfiguration.getSettingsMap();

			for (String key : settingsMap.keySet()) {
				if (key.startsWith("ImportStats")) {
					stringBundler.append("[");
					stringBundler.append(key);
					stringBundler.append("=");
					stringBundler.append(settingsMap.get(key));
					stringBundler.append("]");

				}
			}

			httpServletRequest.setAttribute(
				"liferay-staging:process-info:stats", stringBundler.toString());
		}
	}

	private static final String _PAGE = "/process_info/page.jsp";

	private BackgroundTask _backgroundTask;

	private ExportImportConfiguration _exportImportConfiguration;

}