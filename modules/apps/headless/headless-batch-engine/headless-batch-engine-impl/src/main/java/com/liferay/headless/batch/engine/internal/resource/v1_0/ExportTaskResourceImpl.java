/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.batch.engine.internal.resource.v1_0;

import com.liferay.batch.engine.BatchEngineExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineMultiClassExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.ItemClassRegistry;
import com.liferay.batch.engine.model.BatchEngineExportTask;
import com.liferay.batch.engine.service.BatchEngineExportTaskLocalService;
import com.liferay.headless.batch.engine.dto.v1_0.ExportTask;
import com.liferay.headless.batch.engine.internal.resource.v1_0.util.ParametersUtil;
import com.liferay.headless.batch.engine.resource.v1_0.ExportTaskResource;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.dao.jdbc.OutputBlob;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Ivica Cardic
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/export-task.properties",
	property = "batch.engine=true", scope = ServiceScope.PROTOTYPE,
	service = ExportTaskResource.class
)
public class ExportTaskResourceImpl extends BaseExportTaskResourceImpl {

	@Override
	public ExportTask getExportTask(Long exportTaskId) throws Exception {
		return _toExportTask(
			_batchEngineExportTaskLocalService.getBatchEngineExportTask(
				exportTaskId));
	}

	@Override
	public ExportTask getExportTaskByExternalReferenceCode(
			String externalReferenceCode)
		throws Exception {

		return _toExportTask(
			_batchEngineExportTaskLocalService.
				getBatchEngineExportTaskByExternalReferenceCode(
					externalReferenceCode, contextCompany.getCompanyId()));
	}

	@Override
	public Response getExportTaskByExternalReferenceCodeContent(
			String externalReferenceCode)
		throws Exception {

		BatchEngineExportTask batchEngineExportTask =
			_batchEngineExportTaskLocalService.
				getBatchEngineExportTaskByExternalReferenceCode(
					externalReferenceCode, contextCompany.getCompanyId());

		return _getExportTaskContent(batchEngineExportTask);
	}

	@Override
	public Response getExportTaskContent(Long exportTaskId) throws Exception {
		return _getExportTaskContent(
			_batchEngineExportTaskLocalService.getBatchEngineExportTask(
				exportTaskId));
	}

	@Override
	public ExportTask postExportTask(
		String className, String contentType, String callbackURL,
		String externalReferenceCode, String fieldNames,
		String taskItemDelegateName)
		throws Exception {

		Class<?> clazz = _itemClassRegistry.getItemClass(className);

		if (clazz == null) {
			throw new IllegalArgumentException(
				"Unknown class name: " + className);
		}

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				ExportTaskResourceImpl.class.getName());

		BatchEngineExportTask batchEngineExportTask =
			_batchEngineExportTaskLocalService.addBatchEngineExportTask(
				externalReferenceCode, contextCompany.getCompanyId(),
				contextUser.getUserId(), callbackURL, className,
				StringUtil.upperCase(contentType),
				BatchEngineTaskExecuteStatus.INITIAL.name(),
				_toList(fieldNames),
				ParametersUtil.toParameters(contextUriInfo, _ignoredParameters),
				taskItemDelegateName);

		executorService.submit(
			() -> _batchEngineExportTaskExecutor.execute(
				batchEngineExportTask));

		return _toExportTask(batchEngineExportTask);
	}

	@Override
	public ExportTask postExportTaskComposite(
		String callbackURL, String classNames,
		String externalReferenceCode, Long siteId)
		throws Exception {

		_validateClassNames(classNames);

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				ExportTaskResourceImpl.class.getName());


		Map<String, Serializable> parameters =
			ParametersUtil.toParameters(contextUriInfo, _ignoredParameters);

		if (Validator.isNotNull(siteId)) {
			parameters.put("siteId", siteId);
		}

		parameters.put("classNames", classNames);

		BatchEngineExportTask compositeTask =
			_batchEngineExportTaskLocalService.addBatchEngineExportTask(
				externalReferenceCode, contextCompany.getCompanyId(),
				contextUser.getUserId(), callbackURL, "composite",
				"JSONT",
				BatchEngineTaskExecuteStatus.INITIAL.name(),
				null,
				parameters,
				null);

		executorService.submit(
			() -> {
				_batchEngineMultiClassExportTaskExecutor.execute(compositeTask);
			});

		_log.fatal("--------- Finished -------");

		return _toExportTask(compositeTask);
	}

	private void _validateClassNames(String classNames) {
		String[] classNamesArray = classNames.split("\\,");

		for (String className : classNamesArray) {
			Class<?> clazz = _itemClassRegistry.getItemClass(className);

			if (clazz == null) {
				throw new IllegalArgumentException(
					"Unknown class name: " + className);
			}
		}
	}

	private BatchEngineExportTask _createBatchEngineExportTask(
		String className, String contentType, String callbackURL,
		String externalReferenceCode, String fieldNames,
		String taskItemDelegateName, Long siteId) {

		if (!className.equals("composite")) {
			Class<?> clazz = _itemClassRegistry.getItemClass(className);

			if (clazz == null) {
				throw new IllegalArgumentException(
					"Unknown class name: " + className);
			}
		}

		Map<String, Serializable> parameters =
			ParametersUtil.toParameters(contextUriInfo, _ignoredParameters);

		if (Validator.isNotNull(siteId)) {
			parameters.put("siteId", siteId);
		}

		BatchEngineExportTask batchEngineExportTask =
			_batchEngineExportTaskLocalService.addBatchEngineExportTask(
				externalReferenceCode, contextCompany.getCompanyId(),
				contextUser.getUserId(), callbackURL, className,
				StringUtil.upperCase(contentType),
				BatchEngineTaskExecuteStatus.INITIAL.name(),
				_toList(fieldNames),
				parameters,
				taskItemDelegateName);
		return batchEngineExportTask;
	}

	private Response _getExportTaskContent(
		BatchEngineExportTask batchEngineExportTask) {

		BatchEngineTaskExecuteStatus batchEngineTaskExecuteStatus =
			BatchEngineTaskExecuteStatus.valueOf(
				batchEngineExportTask.getExecuteStatus());

		if (batchEngineTaskExecuteStatus ==
				BatchEngineTaskExecuteStatus.COMPLETED) {

			StreamingOutput streamingOutput =
				outputStream -> StreamUtil.transfer(
					_batchEngineExportTaskLocalService.openContentInputStream(
						batchEngineExportTask.getBatchEngineExportTaskId()),
					outputStream);

			return Response.ok(
				streamingOutput
			).header(
				"content-disposition",
				"attachment; filename=" + StringUtil.randomString() + ".zip"
			).build();
		}

		return Response.status(
			Response.Status.NOT_FOUND
		).build();
	}

	private ExportTask _toExportTask(
		BatchEngineExportTask batchEngineExportTask) {

		return new ExportTask() {
			{
				className = batchEngineExportTask.getClassName();
				contentType = batchEngineExportTask.getContentType();
				endTime = batchEngineExportTask.getEndTime();
				errorMessage = batchEngineExportTask.getErrorMessage();
				executeStatus = ExportTask.ExecuteStatus.create(
					batchEngineExportTask.getExecuteStatus());
				externalReferenceCode =
					batchEngineExportTask.getExternalReferenceCode();
				id = batchEngineExportTask.getBatchEngineExportTaskId();
				processedItemsCount =
					batchEngineExportTask.getProcessedItemsCount();
				startTime = batchEngineExportTask.getStartTime();
				totalItemsCount = batchEngineExportTask.getTotalItemsCount();
			}
		};
	}

	private List<String> _toList(String fieldNamesString) {
		if (Validator.isNull(fieldNamesString)) {
			return Collections.emptyList();
		}

		return Arrays.asList(StringUtil.split(fieldNamesString, ','));
	}

	private static final Set<String> _ignoredParameters = new HashSet<>(
		Arrays.asList("callbackURL", "fieldNames"));

	@Reference
	private BatchEngineExportTaskExecutor _batchEngineExportTaskExecutor;

	@Reference
	private BatchEngineMultiClassExportTaskExecutor
		_batchEngineMultiClassExportTaskExecutor;

	@Reference
	private BatchEngineExportTaskLocalService
		_batchEngineExportTaskLocalService;

	@Reference
	private ItemClassRegistry _itemClassRegistry;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(ExportTaskResourceImpl.class);
}