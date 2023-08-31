/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal;

import com.liferay.batch.engine.BatchEngineExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineMultiClassExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskContentType;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.ItemClassRegistry;
import com.liferay.batch.engine.model.BatchEngineExportTask;
import com.liferay.batch.engine.service.BatchEngineExportTaskLocalService;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.dao.jdbc.OutputBlob;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Validator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Ivica Cardic
 */
@Component(service = BatchEngineMultiClassExportTaskExecutor.class)
public class BatchEngineMultiClassExportTaskExecutorImpl
	implements BatchEngineMultiClassExportTaskExecutor {

	@Override
	public void execute(BatchEngineExportTask batchEngineExportTask) {
		SafeCloseable safeCloseable = CompanyThreadLocal.setWithSafeCloseable(
			batchEngineExportTask.getCompanyId());

		try {
			batchEngineExportTask.setExecuteStatus(
				BatchEngineTaskExecuteStatus.STARTED.toString());
			batchEngineExportTask.setStartTime(new Date());

			_batchEngineExportTaskLocalService.updateBatchEngineExportTask(
				batchEngineExportTask);

			BatchEngineTaskExecutorUtil.execute(
				true, () -> _exportItems(batchEngineExportTask),
				_userLocalService.getUser(batchEngineExportTask.getUserId()));

			_updateBatchEngineExportTask(
				BatchEngineTaskExecuteStatus.COMPLETED, batchEngineExportTask,
				null);
		}
		catch (Throwable throwable) {
			_log.error(
				"Unable to update batch engine export task " +
					batchEngineExportTask,
				throwable);

			try {
				BatchEngineExportTask currentBatchEngineExportTask =
					_batchEngineExportTaskLocalService.getBatchEngineExportTask(
						batchEngineExportTask.getPrimaryKey());

				_updateBatchEngineExportTask(
					BatchEngineTaskExecuteStatus.FAILED,
					currentBatchEngineExportTask, throwable.getMessage());
			}
			catch (PortalException portalException) {
				_log.error(
					"Unable to update batch engine export task",
					portalException);
			}
		}
		finally {

			// LPS-167011 Because of call to _updateBatchEngineImportTask when
			// catching a Throwable

			safeCloseable.close();
		}
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {


	}

	private void _exportItems(BatchEngineExportTask batchEngineMultiClassExportTask)
		throws Exception {

		Map<String, Serializable> params = batchEngineMultiClassExportTask.getParameters();

		String classNames = (String)params.get("classNames");

		String[] classNamesArray = classNames.split("\\,");

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				BatchEngineMultiClassExportTaskExecutor.class.getName());

		List<Future<Long>> futures = new ArrayList<>();

		_log.fatal("--------- Submitting worker tasks -------");

		for (String className : classNamesArray) {
			Map<String, Serializable> parameters = new HashMap<>(params);

			BatchEngineExportTask batchEngineExportTask =
				_batchEngineExportTaskLocalService.addBatchEngineExportTask(
					null, batchEngineMultiClassExportTask.getCompanyId(),
					batchEngineMultiClassExportTask.getUserId(), null, className,
					"JSONT",
					BatchEngineTaskExecuteStatus.INITIAL.name(),
					null,
					parameters,
					null);

			Future<Long> future = executorService.submit(
				() -> {
					_batchEngineExportTaskExecutor.execute(batchEngineExportTask);

					return batchEngineExportTask.getBatchEngineExportTaskId();
				});

			futures.add(future);
		}

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

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		_finalizeExport(taskIds, unsyncByteArrayOutputStream);

		byte[] content = unsyncByteArrayOutputStream.toByteArray();

		batchEngineMultiClassExportTask.setContent(
			new OutputBlob(
				new UnsyncByteArrayInputStream(content), content.length));

		_batchEngineExportTaskLocalService.updateBatchEngineExportTask(batchEngineMultiClassExportTask);
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
				_copyStream(zis, zipOut);
			}

			zis.close();
		}

		zipOut.close();
		fos.close();

		_log.fatal("Composite export finished");

		FileInputStream fis = new FileInputStream(tempFilePath);
		_copyStream(fis, out);
		fis.close();
	}

	private static void _copyStream(InputStream in, OutputStream out)
		throws IOException {
		byte[] bytes = new byte[1024];

		int length;
		while ((length = in.read(bytes)) >= 0) {
			out.write(bytes, 0, length);
		}
	}

	private BatchEngineExportTask _createBatchEngineExportTask(
		BatchEngineExportTask compositeTask,
		String className, String contentType, String callbackURL,
		String externalReferenceCode, Map<String, Serializable> parameters, String fieldNames,
		String taskItemDelegateName, Long siteId) {

		if (Validator.isNotNull(siteId)) {
			parameters.put("siteId", siteId);
		}

		BatchEngineExportTask batchEngineExportTask =
			_batchEngineExportTaskLocalService.addBatchEngineExportTask(
				null, compositeTask.getCompanyId(),
				compositeTask.getUserId(), null, className,
				StringUtil.upperCase(contentType),
				BatchEngineTaskExecuteStatus.INITIAL.name(),
				null,
				parameters,
				taskItemDelegateName);

		return batchEngineExportTask;
	}


	private ZipOutputStream _getZipOutputStream(
			BatchEngineTaskContentType batchEngineTaskContentType,
			UnsyncByteArrayOutputStream unsyncByteArrayOutputStream)
		throws Exception {

		ZipOutputStream zipOutputStream = new ZipOutputStream(
			unsyncByteArrayOutputStream);

		ZipEntry zipEntry = new ZipEntry(
			"export." + batchEngineTaskContentType.getFileExtension());

		zipOutputStream.putNextEntry(zipEntry);

		return zipOutputStream;
	}

	private void _updateBatchEngineExportTask(
		BatchEngineTaskExecuteStatus batchEngineTaskExecuteStatus,
		BatchEngineExportTask batchEngineExportTask, String errorMessage) {

		batchEngineExportTask.setEndTime(new Date());
		batchEngineExportTask.setErrorMessage(errorMessage);
		batchEngineExportTask.setExecuteStatus(
			batchEngineTaskExecuteStatus.toString());

		batchEngineExportTask =
			_batchEngineExportTaskLocalService.updateBatchEngineExportTask(
				batchEngineExportTask);

		BatchEngineTaskCallbackUtil.sendCallback(
			batchEngineExportTask.getCallbackURL(),
			batchEngineExportTask.getExecuteStatus(),
			batchEngineExportTask.getBatchEngineExportTaskId());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineMultiClassExportTaskExecutorImpl.class);

	@Reference
	private BatchEngineExportTaskLocalService
		_batchEngineExportTaskLocalService;

	@Reference
	private ItemClassRegistry _itemClassRegistry;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private BatchEngineExportTaskExecutor _batchEngineExportTaskExecutor;

	@Reference
	private PortalExecutorManager _portalExecutorManager;
}