/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.batch.engine.internal.resource.v1_0;

import com.liferay.batch.engine.BatchEngineExportTaskExecutor;
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

		String[] classNamesArray = classNames.split("\\,");

		BatchEngineExportTask res = null;

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				ExportTaskResourceImpl.class.getName());

		List<Future<Long>> futures = new ArrayList<>();

		_log.fatal("--------- Submitting worker tasks -------");

		for (String cn : classNamesArray) {
			BatchEngineExportTask batchEngineExportTask =
				_createBatchEngineExportTask(cn, "JSONT", callbackURL,
					externalReferenceCode, null, null,
					siteId);

			Future<Long> future = executorService.submit(
				() -> {
					_batchEngineExportTaskExecutor.execute(batchEngineExportTask);

					return batchEngineExportTask.getBatchEngineExportTaskId();
				});

			futures.add(future);
		}

		_log.fatal("--------- Worker tasks submitted -------");

		BatchEngineExportTask compositeTask =
			_createBatchEngineExportTask("composite", "JSONT", callbackURL,
				externalReferenceCode, null, null,
				siteId);

		long compositeTaskId = compositeTask.getBatchEngineExportTaskId();

		executorService.submit(
			() -> {
				_executeCompositeTask(futures, compositeTaskId);
			});

		_log.fatal("--------- Finished -------");

		return _toExportTask(compositeTask);
	}

	private void _executeCompositeTask(List<Future<Long>> futures, long compositeTaskId) {
		_log.fatal("--------- Waiting for " + futures.size() + " tasks to finish -------");
		List<Long> taskIds = new ArrayList<>();

		for (Future<Long> future : futures) {
			try {
				long batchEngineExportTaskId = future.get();
				taskIds.add(batchEngineExportTaskId);
				_log.fatal("--- Finished future: "+future);
			}
			catch (InterruptedException e) {
				_log.error(e);
			}
			catch (ExecutionException e) {
				_log.error(e);
			}
		}

		_log.fatal("--------- All Finished -------");

		try {
			BatchEngineExportTask compTask = _batchEngineExportTaskLocalService.getBatchEngineExportTask(
				compositeTaskId);

			try {
				UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
					new UnsyncByteArrayOutputStream();

				_finalizeExport(taskIds, unsyncByteArrayOutputStream);

				byte[] content = unsyncByteArrayOutputStream.toByteArray();

				compTask.setContent(
					new OutputBlob(
						new UnsyncByteArrayInputStream(content), content.length));

				compTask.setExecuteStatus("COMPLETED");
				_batchEngineExportTaskLocalService.updateBatchEngineExportTask(compTask);
			}
			catch (Exception e) {
				_log.error(e);

				compTask.setExecuteStatus("FAILED");
				_batchEngineExportTaskLocalService.updateBatchEngineExportTask(compTask);
			}
		}
		catch (PortalException e) {
			_log.error(e);
		}
	}

	private void _finalizeExport(List<Long> taskIds,
			UnsyncByteArrayOutputStream out) throws Exception {

		String tempDir =
			SystemProperties.get(SystemProperties.TMP_DIR) +
			"/liferay_export/";
		String tempFilePath = tempDir+"export.zip";

		_log.fatal("Writing temp file: "+tempFilePath);

		File tempDirFile = new File(tempDir);
		if (!tempDirFile.exists()) {
			tempDirFile.mkdirs();
		}

		FileOutputStream fos = new FileOutputStream(tempFilePath);
		ZipOutputStream zipOut = new ZipOutputStream(fos);

		/*InputStream fileIn = new FileInputStream("/home/me/dev/projects/liferay-portal/workspaces/liferay-sample-workspace/client-extensions/liferay-sample-batch/client-extension.yaml");
		zipOut.putNextEntry(new ZipEntry("client-extension.yaml"));
		copyStream(fileIn, zipOut);
		fileIn.close();*/

		String zipDir = "batch/";
		zipOut.putNextEntry(new ZipEntry(zipDir));
		zipOut.closeEntry();

		for (Long taskId : taskIds) {
			BatchEngineExportTask batchEngineExportTask = _batchEngineExportTaskLocalService.getBatchEngineExportTask(taskId);

			_log.fatal("--- Task: "+batchEngineExportTask.getClassName()+" "+batchEngineExportTask.getExecuteStatus()+" "+batchEngineExportTask.getTotalItemsCount());

			String batchClassName = batchEngineExportTask.getClassName();

			ZipInputStream zis = new ZipInputStream(batchEngineExportTask.getContent().getBinaryStream());

			ZipEntry ze = zis.getNextEntry();
			if (ze != null) {
				_log.fatal("Zip file: "+ze.getName() + ", "+ze.getSize());

				String name = ze.getName();
				String prefix = "export.";
				if (name.startsWith(prefix)) {
					name = name.substring(prefix.length());
				}

				String shortClassName = batchClassName;
				int n = shortClassName.lastIndexOf('.');
				if (n>-1) {
					shortClassName = shortClassName.substring(n+1);
				}

				String zipEntryName = shortClassName + "." + name;

				_log.fatal("Zip Entry: \""+zipEntryName+"\"");

				zipOut.putNextEntry(new ZipEntry(zipDir + zipEntryName));
				copyStream(zis, zipOut);
			}

			zis.close();
		}

		zipOut.close();
		fos.close();

		_log.fatal("Composite export finished");

		FileInputStream fis = new FileInputStream(tempFilePath);
		copyStream(fis, out);
		fis.close();
	}

	private static void copyStream(InputStream in, OutputStream out)
		throws IOException {
		byte[] bytes = new byte[1024];

		int length;
		while ((length = in.read(bytes)) >= 0) {
			out.write(bytes, 0, length);
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
	private BatchEngineExportTaskLocalService
		_batchEngineExportTaskLocalService;

	@Reference
	private ItemClassRegistry _itemClassRegistry;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(ExportTaskResourceImpl.class);
}