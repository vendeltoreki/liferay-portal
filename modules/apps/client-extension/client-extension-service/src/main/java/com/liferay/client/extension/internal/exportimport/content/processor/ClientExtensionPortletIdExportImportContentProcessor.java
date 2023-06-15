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

package com.liferay.client.extension.internal.exportimport.content.processor;

import com.liferay.exportimport.content.processor.ExportImportContentProcessor;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.StagedModel;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;

/**
 * @author Vendel Toreki
 */
@Component(
	property = "content.processor.type=EnvironmentSpecificPortletIds",
	service = ExportImportContentProcessor.class
)
public class ClientExtensionPortletIdExportImportContentProcessor
	implements ExportImportContentProcessor<String> {

	@Override
	public String replaceExportContentReferences(
			PortletDataContext portletDataContext, StagedModel stagedModel,
			String content, boolean exportReferencedContent,
			boolean escapeContent)
		throws Exception {

		return content;
	}

	@Override
	public String replaceImportContentReferences(
			PortletDataContext portletDataContext, StagedModel stagedModel,
			String content)
		throws Exception {

		if (!content.contains(_PORTLET_ID_PREFIX)) {
			return content;
		}

		long targetCompanyId = portletDataContext.getCompanyId();

		Map<String, String> replaceStrings = new HashMap<>();

		Pattern pattern = Pattern.compile(
			_PORTLET_ID_PREFIX + "([0-9]+)_([0-9a-z_]{36})");

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			long sourceCompanyId = GetterUtil.getLong(matcher.group(1));

			if ((sourceCompanyId != 0) &&
				(sourceCompanyId != targetCompanyId)) {

				String sourceErc = matcher.group(2);

				String sourceString = StringBundler.concat(
					_PORTLET_ID_PREFIX, sourceCompanyId, StringPool.UNDERLINE,
					sourceErc);
				String targetString = StringBundler.concat(
					_PORTLET_ID_PREFIX, targetCompanyId, StringPool.UNDERLINE,
					sourceErc);

				replaceStrings.put(sourceString, targetString);
			}
		}

		for (Map.Entry<String, String> entry : replaceStrings.entrySet()) {
			content = content.replaceAll(entry.getKey(), entry.getValue());
		}

		return content;
	}

	@Override
	public void validateContentReferences(long groupId, String content) {
	}

	private static final String _PORTLET_ID_PREFIX =
		"com_liferay_client_extension_web_internal_portlet_" +
			"ClientExtensionEntryPortlet_";

}