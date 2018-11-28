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

package com.liferay.journal.internal.upgrade.v1_1_6;

import com.liferay.asset.display.page.constants.AssetDisplayPageConstants;
import com.liferay.asset.display.page.service.AssetDisplayPageEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PortalUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Vendel Toreki
 */
public class UpgradeAssetDisplayPageEntry extends UpgradeProcess {

	public UpgradeAssetDisplayPageEntry(
		AssetDisplayPageEntryLocalService assetDisplayPageEntryLocalService) {

		_assetDisplayPageEntryLocalService = assetDisplayPageEntryLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		updateAssetDisplayPageEntry();
	}

	protected void updateAssetDisplayPageEntry() throws Exception {
		StringBuilder sb = new StringBuilder(8);

		sb.append("select groupId, userId, resourcePrimKey from ");
		sb.append("JournalArticle where JournalArticle.layoutUuid is not ");
		sb.append("null and JournalArticle.layoutUuid != '' and ");
		sb.append("not exists ( select 1 from AssetDisplayPageEntry where ");
		sb.append("AssetDisplayPageEntry.groupId = JournalArticle.groupId ");
		sb.append("and AssetDisplayPageEntry.classNameId = ? and ");
		sb.append("AssetDisplayPageEntry.classPK = ");
		sb.append("JournalArticle.resourcePrimKey )");

		long journalArticleClassNameId = PortalUtil.getClassNameId(
			JournalArticle.class);

		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement ps1 =
				connection.prepareStatement(sb.toString());) {

			ps1.setLong(1, journalArticleClassNameId);

			try (ResultSet rs = ps1.executeQuery()) {
				while (rs.next()) {
					long groupId = rs.getLong("groupId");
					long userId = rs.getLong("userId");
					long resourcePrimKey = rs.getLong("resourcePrimKey");

					_assetDisplayPageEntryLocalService.addAssetDisplayPageEntry(
						userId, groupId, journalArticleClassNameId,
						resourcePrimKey, 0L,
						AssetDisplayPageConstants.TYPE_SPECIFIC,
						new ServiceContext());
				}
			}
		}
	}

	private final AssetDisplayPageEntryLocalService
		_assetDisplayPageEntryLocalService;

}