<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/portlet_list/init.jsp" %>

<%
ExportImportConfiguration eic = new com.liferay.portlet.exportimport.model.impl.ExportImportConfigurationImpl();

eic.setCompanyId(company.getCompanyId());
eic.setGroupId(stagingGroupId);
eic.setSettings("{\"javaClass\":\"java.util.HashMap\",\"map\":{\"timezone\":{\"javaClass\":\"sun.util.calendar.ZoneInfo\",\"serializable\":{\"dstSavings\":0,\"offsets\":null,\"simpleTimeZoneParams\":null,\"rawOffset\":0,\"rawOffsetDiff\":0,\"checksum\":0,\"ID\":\"UTC\",\"transitions\":null,\"willGMTOffsetChange\":false}},\"targetGroupId\":"+liveGroupId+",\"locale\":{\"javaClass\":\"java.util.Locale\",\"locale\":{\"country\":\"US\",\"variant\":\"\",\"language\":\"en\"}},\"privateLayout\":false,\"layoutIds\":[1],\"parameterMap\":{\"javaClass\":\"java.util.HashMap\",\"map\":{\"countReferences\":[\"true\"],\"endDate\":[\"10/11/2023\"],\"checkboxNames\":[\"weeklyDayPos2,weeklyDayPos3,weeklyDayPos4,weeklyDayPos5,weeklyDayPos6,weeklyDayPos7,weeklyDayPos1,DELETE_PORTLET_DATA,DELETIONS,THEME_REFERENCE,LOGO,LAYOUT_SET_SETTINGS,LAYOUT_SET_PROTOTYPE_SETTINGS,DELETE_MISSING_LAYOUTS,DELETE_LAYOUTS,PERMISSIONS\"],\"stagingGroupId\":[\""+stagingGroupId+"\"],\"yearlyDay1\":[\"1\"],\"yearlyDay0\":[\"15\"],\"PORTLET_CONFIGURATION\":[\"true\"],\"currentURL\":[\"http://localhost:8080/group/testsite-staging/~/control_panel/manage?p_p_id=com_liferay_staging_processes_web_portlet_StagingProcessesPortlet&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_com_liferay_staging_processes_web_portlet_StagingProcessesPortlet_mvcRenderCommandName=%2Fstaging_processes%2Fpublish_layouts&_com_liferay_staging_processes_web_portlet_StagingProcessesPortlet_groupId="+stagingGroupId+"&_com_liferay_staging_processes_web_portlet_StagingProcessesPortlet_cmd=publish_to_live&_com_liferay_staging_processes_web_portlet_StagingProcessesPortlet_privateLayout=false&p_p_auth=qatzaBVe\"],\"javax.portlet.action\":[\"/staging_processes/publish_layouts\"],\"endDateAmPm\":[\"1\"],\"endDateDay\":[\"11\"],\"schedulerStartDateYear\":[\"2023\"],\"schedulerStartDateAmPm\":[\"1\"],\"schedulerStartTime\":[\"14:15\"],\"treeId\":[\"stageLayoutsTree33813false0\"],\"USER_ID_STRATEGY\":[\"CURRENT_USER_ID\"],\"LAYOUT_SET_PROTOTYPE_LINK_ENABLED\":[\"false\"],\"schedulerEndTimeDate\":[\"null\"],\"yearlyInterval1\":[\"1\"],\"yearlyInterval0\":[\"1\"],\"endDateType\":[\"0\"],\"endDateYear\":[\"2023\"],\"cmd\":[\"publish_to_live\"],\"PORTLET_SETUP_ALL\":[\"true\"],\"startDateMonth\":[\"9\"],\"startDate\":[\"10/11/2023\"],\"startDateMinute\":[\"49\"],\"PORTLET_DATA\":[\"true\"],\"schedulerStartDate\":[\"10/11/2023\"],\"range\":[\"fromLastPublishDate\"],\"schedulerEndDateAmPm\":[\"1\"],\"yearlyType\":[\"0\"],\"startDateDay\":[\"11\"],\"PERFORM_DIRECT_BINARY_IMPORT\":[\"true\"],\"schedulerEndDate\":[\"10/11/2023\"],\"lastImportUserUuid\":[\"710bb448-0a70-4350-b80b-424a62061b1e\"],\"startTime\":[\"08:49\"],\"DATA_STRATEGY\":[\"DATA_STRATEGY_MIRROR_OVERWRITE\"],\"startDateHour\":[\"8\"],\"UPDATE_LAST_PUBLISH_DATE\":[\"true\"],\"redirect\":[\"http://localhost:8080/group/testsite-staging/~/control_panel/manage?p_p_id=com_liferay_staging_processes_web_portlet_StagingProcessesPortlet&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_com_liferay_staging_processes_web_portlet_StagingProcessesPortlet_mvcRenderCommandName=%2Fstaging_processes%2Fview_processes_list&p_p_auth=qatzaBVe\"],\"DELETE_PORTLET_DATA\":[\"false\"],\"layoutSetBranchName\":[\"\"],\"dailyType\":[\"0\"],\"schedulerEndDateYear\":[\"2023\"],\"startDateTime\":[\"Wed Oct 11 08:49:58 GMT 2023\"],\"yearlyPos\":[\"1\"],\"endTime\":[\"14:15\"],\"endDateMonth\":[\"9\"],\"LAYOUT_SET_PROTOTYPE_SETTINGS\":[\"true\"],\"lastImportUserName\":[\"Test Test\"],\"PERMISSIONS\":[\"false\"],\"monthlyDay0\":[\"15\"],\"groupId\":[\""+stagingGroupId+"\"],\"dailyInterval\":[\"1\"],\"monthlyDay1\":[\"1\"],\"LOGO\":[\"true\"],\"schedulerStartDateHour\":[\"2\"],\"mvcRenderCommandName\":[\"/staging_processes/publish_layouts\"],\"endDateMinute\":[\"15\"],\"endDateHour\":[\"2\"],\"originalCmd\":[\"publish_to_live\"],\"DELETE_MISSING_LAYOUTS\":[\"false\"],\"monthlyPos\":[\"1\"],\"jobName\":[\"\"],\"LAYOUT_SET_SETTINGS\":[\"true\"],\"last\":[\"12\"],\"schedulerStartTimeDate\":[\"null\"],\"formDate\":[\"1697033721460\"],\"PORTLET_DATA_CONTROL_DEFAULT\":[\"true\"],\"monthlyType\":[\"0\"],\"privateLayout\":[\"false\"],\"endDateTime\":[\"Wed Oct 11 14:15:21 GMT 2023\"],\"PORTLET_DATA_com_liferay_exportimport_web_portlet_ChangesetPortlet\":[\"true\"],\"name\":[\"\"],\"schedulerStartDateMonth\":[\"9\"],\"PORTLET_DATA_ALL\":[\"false\"],\"weeklyInterval\":[\"1\"],\"schedulerEndDateMonth\":[\"9\"],\"monthlyInterval0\":[\"1\"],\"schedulerEndTime\":[\"14:15\"],\"monthlyInterval1\":[\"1\"],\"recurrenceType\":[\"7\"],\"DELETE_LAYOUTS\":[\"false\"],\"PORTLET_ARCHIVED_SETUPS_ALL\":[\"true\"],\"PORTLET_CONFIGURATION_ALL\":[\"true\"],\"exportImportConfigurationId\":[\"0\"],\"yearlyMonth1\":[\"0\"],\"yearlyMonth0\":[\"0\"],\"THEME_REFERENCE\":[\"true\"],\"timeZoneId\":[\"UTC\"],\"weeklyDayPos6\":[\"false\"],\"weeklyDayPos5\":[\"false\"],\"weeklyDayPos7\":[\"false\"],\"schedulerEndDateHour\":[\"2\"],\"weeklyDayPos2\":[\"false\"],\"weeklyDayPos1\":[\"false\"],\"weeklyDayPos4\":[\"false\"],\"weeklyDayPos3\":[\"false\"],\"schedule\":[\"false\"],\"schedulerStartDateMinute\":[\"15\"],\"schedulerStartDateDay\":[\"11\"],\"PORTLET_USER_PREFERENCES_ALL\":[\"true\"],\"schedulerEndDateMinute\":[\"15\"],\"schedulerEndDateDay\":[\"11\"],\"startDateYear\":[\"2023\"],\"DELETIONS\":[\"false\"],\"startDateAmPm\":[\"0\"]}},\"userId\":20123,\"sourceGroupId\":"+stagingGroupId+"}}");

List<String> res = ExportImportLocalServiceUtil.collectExportLayoutsReferences(eic);

for (String ref : res) {
%>
<div>REF: <%= ref %></div>

<%
}
%>


<liferay-util:buffer
	var="html"
>

	<%
	DateRange dateRange = null;

	for (Portlet portlet : portlets) {
		if (!type.equals(Constants.EXPORT) && (liveGroup != null) && !liveGroup.isStagedPortlet(portlet.getRootPortletId())) {
			continue;
		}

		if (!GroupCapabilityUtil.isSupportsPortlet(liveGroup, portlet)) {
			continue;
		}

		PortletDataHandler portletDataHandler = portlet.getPortletDataHandlerInstance();

		Class<?> portletDataHandlerClass = portletDataHandler.getClass();

		String portletDataHandlerClassName = portletDataHandlerClass.getName();

		if (portletDataHandlerClassNames.contains(portletDataHandlerClassName)) {
			continue;
		}

		portletDataHandlerClassNames.add(portletDataHandlerClassName);

		String portletTitle = PortalUtil.getPortletTitle(portlet, application, locale);

		PortletDataHandlerControl[] exportControls = portletDataHandler.getExportControls();
		PortletDataHandlerControl[] metadataControls = portletDataHandler.getExportMetadataControls();
		PortletDataHandlerControl[] stagingControls = portletDataHandler.getStagingControls();

		if (!type.equals(Constants.EXPORT) && liveGroup.isStagedPortlet(portlet.getRootPortletId())) {
			exportControls = stagingControls;
		}

		if (ArrayUtil.isEmpty(exportControls) && ArrayUtil.isEmpty(metadataControls)) {
			continue;
		}

		if (useRequestValues) {
			dateRange = ExportImportDateUtil.getDateRange(renderRequest, exportGroupId, privateLayout, 0, portlet.getRootPortletId(), defaultRange);
		}
		else {
			dateRange = ExportImportDateUtil.getDateRange(exportImportConfiguration, portlet.getRootPortletId());
		}

		PortletDataContext portletDataContext = PortletDataContextFactoryUtil.createPreparePortletDataContext(company.getCompanyId(), exportGroupId, (range != null) ? range : defaultRange, dateRange.getStartDate(), dateRange.getEndDate());

		portletDataHandler.prepareManifestSummary(portletDataContext);

		ManifestSummary manifestSummary = portletDataContext.getManifestSummary();

		long exportModelCount = portletDataHandler.getExportModelCount(manifestSummary);

		long modelDeletionCount = manifestSummary.getModelDeletionCount(portletDataHandler.getDeletionSystemEventStagedModelTypes());

		boolean displayCounts = (exportModelCount > 0) || (modelDeletionCount > 0);

		if (!type.equals(Constants.EXPORT)) {
			UnicodeProperties liveGroupTypeSettingsUnicodeProperties = liveGroup.getTypeSettingsProperties();

			displayCounts = displayCounts && GetterUtil.getBoolean(liveGroupTypeSettingsUnicodeProperties.getProperty(StagingUtil.getStagedPortletId(portlet.getRootPortletId())), portletDataHandler.isPublishToLiveByDefault());
		}

		if (!displayCounts && !showAllPortlets) {
			continue;
		}

		boolean showPortletDataInput = MapUtil.getBoolean(parameterMap, PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE + portlet.getPortletId(), portletDataHandler.isPublishToLiveByDefault()) || MapUtil.getBoolean(parameterMap, PortletDataHandlerKeys.PORTLET_DATA_ALL);
	%>

		<li class="tree-item <%= ((exportModelCount > 0) || showAllPortlets) ? StringPool.BLANK : "deletions" %>">
			<liferay-staging:checkbox
				checked="<%= showPortletDataInput %>"
				deletions="<%= modelDeletionCount %>"
				disabled="<%= disableInputs %>"
				items="<%= exportModelCount %>"
				label="<%= portletTitle %>"
				name="<%= PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE + portlet.getPortletId() %>"
			/>

			<div class="<%= (disableInputs && showPortletDataInput) ? StringPool.BLANK : "hide " %>" id="<portlet:namespace />content_<%= portlet.getPortletId() %>">
				<ul class="lfr-tree list-unstyled">
					<li class="tree-item">
						<aui:fieldset cssClass="portlet-type-data-section" label="<%= portletTitle %>">
							<c:if test="<%= exportControls != null %>">
								<c:choose>
									<c:when test="<%= type.equals(Constants.EXPORT) %>">

										<%
										request.setAttribute("render_controls.jsp-action", Constants.EXPORT);
										request.setAttribute("render_controls.jsp-childControl", false);
										request.setAttribute("render_controls.jsp-controls", exportControls);
										request.setAttribute("render_controls.jsp-disableInputs", disableInputs);
										request.setAttribute("render_controls.jsp-manifestSummary", manifestSummary);
										request.setAttribute("render_controls.jsp-parameterMap", parameterMap);
										request.setAttribute("render_controls.jsp-portletDisabled", !portletDataHandler.isPublishToLiveByDefault());
										request.setAttribute("render_controls.jsp-portletId", portlet.getPortletId());
										%>

										<aui:field-wrapper label='<%= ArrayUtil.isNotEmpty(metadataControls) ? "content" : StringPool.BLANK %>'>
											<ul class="lfr-tree list-unstyled">
												<liferay-util:include page="/portlet_list/render_controls.jsp" servletContext="<%= application %>" />
											</ul>
										</aui:field-wrapper>
									</c:when>
									<c:when test="<%= (liveGroup != null) && liveGroup.isStagedPortlet(portlet.getRootPortletId()) %>">

										<%
										request.setAttribute("render_controls.jsp-action", Constants.PUBLISH);
										request.setAttribute("render_controls.jsp-childControl", false);
										request.setAttribute("render_controls.jsp-controls", exportControls);
										request.setAttribute("render_controls.jsp-disableInputs", disableInputs);
										request.setAttribute("render_controls.jsp-manifestSummary", manifestSummary);
										request.setAttribute("render_controls.jsp-parameterMap", parameterMap);
										request.setAttribute("render_controls.jsp-portletDisabled", !portletDataHandler.isPublishToLiveByDefault());
										request.setAttribute("render_controls.jsp-portletId", portlet.getPortletId());
										%>

										<aui:field-wrapper label='<%= ArrayUtil.isNotEmpty(metadataControls) ? "content" : StringPool.BLANK %>'>
											<ul class="lfr-tree list-unstyled">
												<liferay-util:include page="/portlet_list/render_controls.jsp" servletContext="<%= application %>" />
											</ul>
										</aui:field-wrapper>
									</c:when>
								</c:choose>
							</c:if>

							<c:if test="<%= metadataControls != null %>">

								<%
								for (PortletDataHandlerControl metadataControl : metadataControls) {
									if (displayedControls.contains(metadataControl.getControlName())) {
										continue;
									}

									displayedControls.add(metadataControl.getControlName());

									PortletDataHandlerBoolean control = (PortletDataHandlerBoolean)metadataControl;

									PortletDataHandlerControl[] childrenControls = control.getChildren();
								%>

									<c:if test="<%= ArrayUtil.isNotEmpty(childrenControls) %>">

										<%
										request.setAttribute("render_controls.jsp-controls", childrenControls);
										request.setAttribute("render_controls.jsp-portletId", portlet.getPortletId());
										%>

										<aui:field-wrapper label="content-metadata">
											<ul class="lfr-tree list-unstyled">
												<liferay-util:include page="/portlet_list/render_controls.jsp" servletContext="<%= application %>" />
											</ul>
										</aui:field-wrapper>
									</c:if>

								<%
								}
								%>

							</c:if>
						</aui:fieldset>
					</li>
				</ul>
			</div>

			<%
			String portletId = portlet.getPortletId();

			if (!type.equals(Constants.EXPORT)) {
				portletId = portlet.getRootPortletId();
			}
			%>

			<ul class="hide" id="<portlet:namespace />showChangeContent_<%= portlet.getPortletId() %>">
				<li>
					<span class="selected-labels" id="<portlet:namespace />selectedContent_<%= portlet.getPortletId() %>"></span>

					<span <%= !disableInputs ? StringPool.BLANK : "class=\"hide\"" %>>
						<aui:a
							cssClass="content-link modify-link"
							data='<%=
								HashMapBuilder.<String, Object>put(
									"portletid", portletId
								).put(
									"portlettitle", portletTitle
								).build()
							%>'
							href="javascript:void(0);"
							id='<%= "contentLink_" + portlet.getPortletId() %>'
							label="change"
							method="get"
						/>
					</span>
				</li>
			</ul>

			<aui:script>
				Liferay.Util.toggleBoxes(
					'<portlet:namespace /><%= PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE + portlet.getPortletId() %>',
					'<portlet:namespace />showChangeContent<%= StringPool.UNDERLINE + portlet.getPortletId() %>'
				);
			</aui:script>
		</li>

	<%
	}
	%>

</liferay-util:buffer>

<%
html = html.trim();
%>

<ul class="portlet-list <%= html.isEmpty() ? "hide" : "" %>">
	<%= html %>
</ul>

<c:if test="<%= type.equals(Constants.EXPORT) %>">
	<aui:fieldset cssClass="content-options" label="for-each-of-the-selected-content-types,-export-their">
		<span class="selected-labels" id="<portlet:namespace />selectedContentOptions"></span>

		<span <%= !disableInputs ? StringPool.BLANK : "class=\"hide\"" %>>
			<aui:a cssClass="modify-link" href="javascript:void(0);" id="contentOptionsLink" label="change" method="get" />
		</span>

		<div class="hide" id="<portlet:namespace />contentOptions">
			<ul class="lfr-tree list-unstyled">
				<li class="tree-item">
					<aui:input disabled="<%= disableInputs %>" label="comments" name="<%= PortletDataHandlerKeys.COMMENTS %>" type="checkbox" value="<%= MapUtil.getBoolean(parameterMap, PortletDataHandlerKeys.COMMENTS, true) %>" />

					<aui:input disabled="<%= disableInputs %>" label="ratings" name="<%= PortletDataHandlerKeys.RATINGS %>" type="checkbox" value="<%= MapUtil.getBoolean(parameterMap, PortletDataHandlerKeys.RATINGS, true) %>" />
				</li>
			</ul>
		</div>
	</aui:fieldset>
</c:if>