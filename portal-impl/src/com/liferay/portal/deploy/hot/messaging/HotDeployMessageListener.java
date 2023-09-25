/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.deploy.hot.messaging;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Message;

/**
 * @author Brian Wing Shun Chan
 * @author Eduardo Lundgren
 */
public class HotDeployMessageListener extends BaseMessageListener {

	@Override
	protected void doReceive(Message message) throws Exception {
		if (!_log.isDebugEnabled()) {
			return;
		}

		String command = message.getString("command");
		String servletContextName = message.getString("servletContextName");

		if (command.equals("deploy")) {
			_log.debug(servletContextName + " was deployed");
		}
		else if (command.equals("undeploy")) {
			_log.debug(servletContextName + " was undeployed");
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HotDeployMessageListener.class);

}