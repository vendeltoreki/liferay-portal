package com.liferay.batch.engine.internal;

import java.util.HashSet;
import java.util.Set;

public class BatchEngineExportDataContext {
	public void addAttachment(long fileEntryId) {
		_attachemntIds.add(fileEntryId);
	}

	private Set<Long> _attachemntIds = new HashSet<>();

	public Iterable<? extends Long> getFileEntryIdSet() {
		return _attachemntIds;
	}
}
