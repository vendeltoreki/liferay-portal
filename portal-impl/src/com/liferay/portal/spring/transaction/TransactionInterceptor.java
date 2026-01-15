/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.spring.transaction;

import com.liferay.portal.kernel.aop.AopMethodInvocation;
import com.liferay.portal.kernel.aop.ChainableMethodAdvice;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.transaction.Transactional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.util.Map;

import com.liferay.portal.kernel.util.StackTraceUtil;
import com.liferay.portal.kernel.util.StringUtil;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * @author Shuyang Zhou
 */
public class TransactionInterceptor extends ChainableMethodAdvice {

	public TransactionInterceptor(TransactionExecutor transactionExecutor) {
		_transactionExecutor = transactionExecutor;
	}

	@Override
	public TransactionAttributeAdapter createMethodContext(
		Class<?> targetClass, Method method,
		Map<Class<? extends Annotation>, Annotation> annotations) {

		Transactional transactional = targetClass.getAnnotation(
			Transactional.class);

		if ((transactional != null) && !transactional.enabled()) {
			annotations.put(Transactional.class, transactional);

			return null;
		}

		transactional = (Transactional)annotations.get(Transactional.class);

		TransactionAttribute transactionAttribute =
			TransactionAttributeBuilder.build(transactional);

		if (transactionAttribute == null) {
			return null;
		}

		return new TransactionAttributeAdapter(transactionAttribute);
	}

	@Override
	public Object invoke(
			AopMethodInvocation aopMethodInvocation, Object[] arguments)
		throws Throwable {

		String id = StringUtil.randomString(8);
		_log("TRANSACTION START "+id);

		TransactionAttributeAdapter transactionAttributeAdapter =
			aopMethodInvocation.getAdviceMethodContext();

		TransactionStatusAdapter transactionStatusAdapter =
			_transactionExecutor.start(transactionAttributeAdapter);

		Object returnValue = null;

		try {
			returnValue = aopMethodInvocation.proceed(arguments);
		}
		catch (Throwable throwable) {
			_log("TRANSACTION ROLLBACK "+id);

			_transactionExecutor.rollback(
				throwable, transactionAttributeAdapter,
				transactionStatusAdapter);
		}

		_log("TRANSACTION COMMIT "+id);

		_transactionExecutor.commit(
			transactionAttributeAdapter, transactionStatusAdapter);

		return returnValue;
	}

	private void _log(String message) {
		Throwable throwable = new Throwable();

		String stackTrace = StackTraceUtil.getStackTrace(throwable);

		if (!stackTrace.contains("LayoutImportBackgroundTaskExecutor")) {
			return;
		}

		if (_log.isDebugEnabled()) {
			_log.debug(message);
		}

		if (_log.isTraceEnabled()) {
			_log.trace(message, throwable);
		}
	}
	private final TransactionExecutor _transactionExecutor;

	private static final Log _log = LogFactoryUtil.getLog(
		TransactionInterceptor.class);
}