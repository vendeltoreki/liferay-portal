/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal;

import com.liferay.batch.engine.BatchEngineExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineMultiClassExportTaskExecutor;
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
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.SystemProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
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

	private void _exportItems(
			BatchEngineExportTask batchEngineMultiClassExportTask)
		throws Exception {

		Map<String, Serializable> multiClassTaskParams =
			batchEngineMultiClassExportTask.getParameters();

		String[] classNames = StringUtil.split(
			MapUtil.getString(multiClassTaskParams, "classNames"));

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				BatchEngineMultiClassExportTaskExecutor.class.getName());

		List<Future<Long>> futures = new ArrayList<>();

		if (_log.isDebugEnabled()) {
			_log.debug("Submitting worker tasks");
		}

		for (String className : classNames) {
			Map<String, Serializable> parameters = new HashMap<>(
				multiClassTaskParams);

			BatchEngineExportTask batchEngineExportTask =
				_batchEngineExportTaskLocalService.addBatchEngineExportTask(
					null, batchEngineMultiClassExportTask.getCompanyId(),
					batchEngineMultiClassExportTask.getUserId(), null,
					className, "JSONT",
					BatchEngineTaskExecuteStatus.INITIAL.name(), null,
					parameters, null);

			Future<Long> future = executorService.submit(
				() -> {
					_batchEngineExportTaskExecutor.execute(
						batchEngineExportTask);

					return batchEngineExportTask.getBatchEngineExportTaskId();
				});

			futures.add(future);
		}

		List<Long> taskIds = _waitForTasksToFinish(futures);

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		_finalizeExport(
			taskIds, unsyncByteArrayOutputStream,
			batchEngineMultiClassExportTask);

		byte[] content = unsyncByteArrayOutputStream.toByteArray();

		batchEngineMultiClassExportTask.setContent(
			new OutputBlob(
				new UnsyncByteArrayInputStream(content), content.length));

		_batchEngineExportTaskLocalService.updateBatchEngineExportTask(
			batchEngineMultiClassExportTask);
	}

	private void _finalizeExport(
			List<Long> taskIds,
			UnsyncByteArrayOutputStream unsyncByteArrayOutputStream,
			BatchEngineExportTask batchEngineMultiClassExportTask)
		throws Exception {

		String tempDir =
			SystemProperties.get(SystemProperties.TMP_DIR) + "/liferay_export/";

		String tempFilePath = tempDir + "export.zip";

		if (_log.isDebugEnabled()) {
			_log.debug("Writing temp file: " + tempFilePath);
		}

		File tempDirFile = new File(tempDir);

		if (!tempDirFile.exists()) {
			tempDirFile.mkdirs();
		}

		FileOutputStream fileOutputStream = new FileOutputStream(tempFilePath);

		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

		String zipDir = "batch/";

		zipOutputStream.putNextEntry(new ZipEntry(zipDir));

		zipOutputStream.closeEntry();

		int sumProcessedCount = 0;
		int sumTotalCount = 0;

		for (Long taskId : taskIds) {
			BatchEngineExportTask batchEngineExportTask =
				_batchEngineExportTaskLocalService.getBatchEngineExportTask(
					taskId);

			sumProcessedCount += batchEngineExportTask.getProcessedItemsCount();
			sumTotalCount += batchEngineExportTask.getTotalItemsCount();

			ZipInputStream zipInputStream = new ZipInputStream(
				batchEngineExportTask.getContent(
				).getBinaryStream());

			ZipEntry zipEntry = zipInputStream.getNextEntry();

			if (zipEntry != null) {
				String zipEntryName = _getZipEntryName(
					batchEngineExportTask.getClassName(), zipEntry.getName());

				zipOutputStream.putNextEntry(
					new ZipEntry(zipDir + zipEntryName));

				StreamUtil.transfer(
					zipInputStream, StreamUtil.uncloseable(zipOutputStream));
			}

			zipInputStream.close();
		}

		zipOutputStream.close();

		fileOutputStream.close();

		batchEngineMultiClassExportTask.setProcessedItemsCount(
			sumProcessedCount);
		batchEngineMultiClassExportTask.setTotalItemsCount(sumTotalCount);

		FileInputStream fileInputStream = new FileInputStream(tempFilePath);

		StreamUtil.transfer(fileInputStream, unsyncByteArrayOutputStream);

		fileInputStream.close();
	}

	private String _getZipEntryName(String className, String originalFileName) {
		String prefix = "export.";

		if (originalFileName.startsWith(prefix)) {
			originalFileName = originalFileName.substring(prefix.length());
		}

		String shortClassName = className;

		int n = shortClassName.lastIndexOf('.');

		if (n > -1) {
			shortClassName = shortClassName.substring(n + 1);
		}

		return shortClassName + "." + originalFileName;
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

	private List<Long> _waitForTasksToFinish(List<Future<Long>> futures) {
		if (_log.isDebugEnabled()) {
			_log.debug("Waiting for " + futures.size() + " tasks to finish");
		}

		List<Long> taskIds = new ArrayList<>();

		for (Future<Long> future : futures) {
			try {
				long batchEngineExportTaskId = future.get();

				taskIds.add(batchEngineExportTaskId);

				if (_log.isDebugEnabled()) {
					_log.debug("Finished future: " + future);
				}
			}
			catch (Exception exception) {
				_log.error(exception);
			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Worker tasks finished");
		}

		return taskIds;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineMultiClassExportTaskExecutorImpl.class);

	@Reference
	private BatchEngineExportTaskExecutor _batchEngineExportTaskExecutor;

	@Reference
	private BatchEngineExportTaskLocalService
		_batchEngineExportTaskLocalService;

	@Reference
	private ItemClassRegistry _itemClassRegistry;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	@Reference
	private UserLocalService _userLocalService;

}