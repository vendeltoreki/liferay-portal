/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.background.task;

import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalServiceUtil;
import com.liferay.batch.engine.service.BatchEngineImportTaskService;
import com.liferay.batch.engine.service.BatchEngineImportTaskServiceUtil;
import com.liferay.exportimport.kernel.exception.ExportImportIOException;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportLocalService;
import com.liferay.exportimport.report.model.ExportImportReportEntryTable;
import com.liferay.exportimport.report.service.ExportImportReportEntryLocalService;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import com.liferay.staging.StagingGroupHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Daniel Kocsis
 * @author Akos Thurzo
 */
@Component(
	property = "background.task.executor.class.name=com.liferay.exportimport.internal.background.task.LayoutImportBackgroundTaskExecutor",
	service = BackgroundTaskExecutor.class
)
public class LayoutImportBackgroundTaskExecutor
	extends BaseExportImportBackgroundTaskExecutor {

	public LayoutImportBackgroundTaskExecutor() {
		setBackgroundTaskStatusMessageTranslator(
			new LayoutExportImportBackgroundTaskStatusMessageTranslator());

		// Isolation level guarantees this will be serial in a group

		setIsolationLevel(BackgroundTaskConstants.ISOLATION_LEVEL_GROUP);
	}

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		ExportImportConfiguration exportImportConfiguration =
			getExportImportConfiguration(backgroundTask);

		List<FileEntry> attachmentsFileEntries =
			backgroundTask.getAttachmentsFileEntries();

		File file = null;

		_testTransactions(backgroundTask.getCompanyId());

		for (FileEntry attachmentsFileEntry : attachmentsFileEntries) {
			try {
				file = FileUtil.createTempFile("lar");

				FileUtil.write(file, attachmentsFileEntry.getContentStream());

				TransactionInvokerUtil.invoke(
					transactionConfig,
					new LayoutImportCallable(exportImportConfiguration, file));
			}
			catch (IOException ioException) {
				ExportImportIOException exportImportIOException =
					new ExportImportIOException(
						LayoutImportBackgroundTaskExecutor.class.getName(),
						ioException);

				if (Validator.isNotNull(attachmentsFileEntry.getFileName())) {
					exportImportIOException.setFileName(
						attachmentsFileEntry.getFileName());
					exportImportIOException.setType(
						ExportImportIOException.LAYOUT_IMPORT_FILE);
				}
				else {
					exportImportIOException.setType(
						ExportImportIOException.LAYOUT_IMPORT);
				}

				throw exportImportIOException;
			}
			catch (Throwable throwable) {
				throw new SystemException(throwable);
			}
			finally {
				FileUtil.delete(file);

				PortletFileRepositoryUtil.deletePortletFileEntry(
					attachmentsFileEntry.getFileEntryId());
			}
		}

		int count = _exportImportReportEntryLocalService.dslQueryCount(
			DSLQueryFactoryUtil.count(
			).from(
				ExportImportReportEntryTable.INSTANCE
			).where(
				ExportImportReportEntryTable.INSTANCE.companyId.eq(
					exportImportConfiguration.getCompanyId()
				).and(
					ExportImportReportEntryTable.INSTANCE.
						exportImportConfigurationId.eq(
							exportImportConfiguration.
								getExportImportConfigurationId())
				)
			));

		if (count > 0) {
			return BackgroundTaskResult.COMPLETED_WITH_ERRORS;
		}

		return BackgroundTaskResult.SUCCESS;
	}

	private void _testTransactions(long companyId) {
		try {
			TransactionInvokerUtil.invoke(
				transactionConfig,
				new Callable<Void>() {
					@Override
					public Void call() throws Exception {

			///  TEST -1
			BatchEngineImportTask beit1 =
				_batchEngineImportTaskService.addBatchEngineImportTask(
					null, companyId, 0, 100,
					null, "ASDFGH",
					new byte[0], "JSON",
					BatchEngineTaskExecuteStatus.COMPLETED.name(),
					Collections.emptyMap(),
					BatchEngineImportTaskConstants.
						IMPORT_STRATEGY_ON_ERROR_CONTINUE,
					BatchEngineTaskOperation.CREATE.name(),
					Collections.emptyMap(),
					"QWERTY");

			///  TEST 0
						beit1.setTotalItemsCount(8);
						_batchEngineImportTaskService.updateBatchEngineImportTask(
							beit1);

						return null;
					}
				});

			///  TEST 1
			TransactionInvokerUtil.invoke(
				transactionConfig,
				new Callable<Void>() {
					@Override
					public Void call() throws Exception {

					BatchEngineImportTask beit2 =
						_batchEngineImportTaskService.addBatchEngineImportTask(
							null, companyId, 0, 100,
							null, "ASDFGH",
							new byte[0], "JSON",
							BatchEngineTaskExecuteStatus.COMPLETED.name(),
							Collections.emptyMap(),
							BatchEngineImportTaskConstants.
								IMPORT_STRATEGY_ON_ERROR_CONTINUE,
							BatchEngineTaskOperation.CREATE.name(),
							Collections.emptyMap(),
							"QWERTY");

						return null;
					}
				});

		} catch (Throwable e) {
			_log.error(e);
		}
	}

	private void _testTransactions2(long companyId) {
		try {
			long userId = UserLocalServiceUtil.getGuestUserId(companyId);

			Random random = new Random();

			Group group1 = GroupLocalServiceUtil.addGroup(
				StringUtil.randomString(8), userId,
				GroupConstants.DEFAULT_PARENT_GROUP_ID,
				StringUtil.randomString(8), random.nextLong(),
				GroupConstants.DEFAULT_LIVE_GROUP_ID, null, null,
				GroupConstants.TYPE_SITE_RESTRICTED, null, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
				StringUtil.randomString(8), false, false, true, null);

			group1.setFriendlyURL(StringUtil.randomString(8));
			GroupLocalServiceUtil.updateGroup(group1);

			Group group2 = GroupLocalServiceUtil.addGroup(
				StringUtil.randomString(8), userId,
				GroupConstants.DEFAULT_PARENT_GROUP_ID,
				StringUtil.randomString(8), random.nextLong(),
				GroupConstants.DEFAULT_LIVE_GROUP_ID, null, null,
				GroupConstants.TYPE_SITE_RESTRICTED, null, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
				StringUtil.randomString(8), false, false, true, null);

		} catch (Exception e) {
			_log.error(e);
		}
	}

	@Reference
	private BatchEngineImportTaskService _batchEngineImportTaskService;

	@Reference
	private BatchEngineImportTaskLocalService _batchEngineImportTaskLocalService;

	@Reference
	private ExportImportLocalService _exportImportLocalService;

	@Reference
	private ExportImportReportEntryLocalService
		_exportImportReportEntryLocalService;

	private class LayoutImportCallable implements Callable<Void> {

		public LayoutImportCallable(
			ExportImportConfiguration exportImportConfiguration, File file) {

			_exportImportConfiguration = exportImportConfiguration;
			_file = file;
		}

		@Override
		public Void call() throws PortalException {
			_testTransactions(_exportImportConfiguration.getCompanyId());

			_exportImportLocalService.importLayoutsDataDeletions(
				_exportImportConfiguration, _file);

			_exportImportLocalService.importLayouts(
				_exportImportConfiguration, _file);

			return null;
		}

		private final ExportImportConfiguration _exportImportConfiguration;
		private final File _file;

	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutImportBackgroundTaskExecutor.class);
}