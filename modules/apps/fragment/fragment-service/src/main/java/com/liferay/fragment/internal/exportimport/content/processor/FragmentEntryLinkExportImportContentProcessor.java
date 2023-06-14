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

package com.liferay.fragment.internal.exportimport.content.processor;

import com.liferay.exportimport.content.processor.ExportImportContentProcessor;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.StagedModel;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(
	property = "model.class.name=com.liferay.fragment.model.FragmentEntryLink",
	service = ExportImportContentProcessor.class
)
public class FragmentEntryLinkExportImportContentProcessor
	implements ExportImportContentProcessor<String> {

	@Override
	public String replaceExportContentReferences(
			PortletDataContext portletDataContext, StagedModel stagedModel,
			String content, boolean exportReferencedContent,
			boolean escapeContent)
		throws Exception {

		FragmentEntryLink fragmentEntryLink = (FragmentEntryLink)stagedModel;

		if (fragmentEntryLink.isTypePortlet()) {
			return content;
		}

		content =
			_dlReferencesExportImportContentProcessor.
				replaceExportContentReferences(
					portletDataContext, stagedModel, content,
					exportReferencedContent, escapeContent);
		content =
			_layoutReferencesExportImportContentProcessor.
				replaceExportContentReferences(
					portletDataContext, stagedModel, content,
					exportReferencedContent, escapeContent);

		JSONObject editableValuesJSONObject = _jsonFactory.createJSONObject(
			content);

		for (ExportImportContentProcessor<JSONObject>
				exportImportContentProcessor :
					_fragmentEntryLinkEditableValuesExportImportProcessors) {

			editableValuesJSONObject =
				exportImportContentProcessor.replaceExportContentReferences(
					portletDataContext, stagedModel, editableValuesJSONObject,
					exportReferencedContent, escapeContent);
		}

		return editableValuesJSONObject.toString();
	}

	@Override
	public String replaceImportContentReferences(
			PortletDataContext portletDataContext, StagedModel stagedModel,
			String content)
		throws Exception {

		FragmentEntryLink fragmentEntryLink = (FragmentEntryLink)stagedModel;

		if (fragmentEntryLink.isTypePortlet()) {
			return _replacePortletIds(content);
		}

		content =
			_dlReferencesExportImportContentProcessor.
				replaceImportContentReferences(
					portletDataContext, stagedModel, content);
		content =
			_layoutReferencesExportImportContentProcessor.
				replaceImportContentReferences(
					portletDataContext, stagedModel, content);

		JSONObject editableValuesJSONObject = _jsonFactory.createJSONObject(
			content);

		for (String fragmentEntryProcessorKey :
				_FRAGMENT_ENTRY_PROCESSOR_KEYS) {

			JSONObject fragmentEntryProcessorJSONObject =
				editableValuesJSONObject.getJSONObject(
					fragmentEntryProcessorKey);

			if ((fragmentEntryProcessorJSONObject != null) &&
				(fragmentEntryProcessorJSONObject.length() == 0)) {

				editableValuesJSONObject.remove(fragmentEntryProcessorKey);
			}
		}

		for (ExportImportContentProcessor<JSONObject>
				exportImportContentProcessor :
					_fragmentEntryLinkEditableValuesExportImportProcessors) {

			editableValuesJSONObject =
				exportImportContentProcessor.replaceImportContentReferences(
					portletDataContext, stagedModel, editableValuesJSONObject);
		}

		return editableValuesJSONObject.toString();
	}

	private static String _replacePortletIds(String content) {
		String portletIdPrefix = "com_liferay_client_extension_web_internal_portlet_ClientExtensionEntryPortlet_";

		if (!content.contains(portletIdPrefix)) {
			return content;
		}
		
		long targetCompanyId = CompanyThreadLocal.getCompanyId();
		
		Map<String,String> replaceStrings = new HashMap<>();
		
		Pattern p = Pattern.compile(portletIdPrefix+"([0-9]+)_([0-9a-z_]{36})");
		Matcher m = p.matcher(content);
		while (m.find()) {
			long sourceCompanyId = Long.valueOf(m.group(1));
			String sourceErc = m.group(2);

			System.out.println("companyId="+sourceCompanyId+", erc="+sourceErc);

			if (sourceCompanyId != targetCompanyId) {
				String sourceString = portletIdPrefix + String.valueOf(sourceCompanyId) + "_" +sourceErc;
				String targetString = portletIdPrefix + String.valueOf(targetCompanyId) + "_" +sourceErc;
				
				replaceStrings.put(sourceString, targetString);
			}
		}

		for (Map.Entry<String, String> entry : replaceStrings.entrySet()) {
			content = content.replaceAll(entry.getKey(), entry.getValue());
		}
		
		return content;
	}

	@Override
	public void validateContentReferences(long groupId, String content)
		throws PortalException {
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_fragmentEntryLinkEditableValuesExportImportProcessors =
			ServiceTrackerListFactory.open(
				bundleContext,
				(Class<ExportImportContentProcessor<JSONObject>>)
					(Class<?>)ExportImportContentProcessor.class,
				"(content.processor.type=FragmentEntryLinkEditableValues)");
	}

	private static final String[] _FRAGMENT_ENTRY_PROCESSOR_KEYS = {
		FragmentEntryProcessorConstants.
			KEY_BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR,
		FragmentEntryProcessorConstants.KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
		FragmentEntryProcessorConstants.KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR
	};

	@Reference(target = "(content.processor.type=DLReferences)")
	private ExportImportContentProcessor<String>
		_dlReferencesExportImportContentProcessor;

	private ServiceTrackerList<ExportImportContentProcessor<JSONObject>>
		_fragmentEntryLinkEditableValuesExportImportProcessors;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference(target = "(content.processor.type=LayoutReferences)")
	private ExportImportContentProcessor<String>
		_layoutReferencesExportImportContentProcessor;

}