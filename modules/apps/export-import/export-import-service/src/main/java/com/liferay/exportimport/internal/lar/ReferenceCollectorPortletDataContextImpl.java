package com.liferay.exportimport.internal.lar;

import com.liferay.portal.kernel.lock.LockManager;

import java.util.ArrayList;
import java.util.List;

public class ReferenceCollectorPortletDataContextImpl
	extends PortletDataContextImpl {

	public ReferenceCollectorPortletDataContextImpl(LockManager lockManager) {
		super(lockManager);
	}

	public List<String> getReferences() {
		return _references;
	}

	private final List<String> _references = new ArrayList<>();

}