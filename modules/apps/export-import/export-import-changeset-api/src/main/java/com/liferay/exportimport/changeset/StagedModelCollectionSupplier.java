package com.liferay.exportimport.changeset;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

import com.liferay.portal.kernel.model.StagedModel;

public class StagedModelCollectionSupplier implements Supplier<Collection<? extends StagedModel>>, Serializable {

	@Override
	public Collection<StagedModel> get() {
		return _stagedModelCollection;
	}

	public StagedModelCollectionSupplier(Collection<StagedModel> stagedModelCollection) {
		super();
		this._stagedModelCollection = stagedModelCollection;
	}

	private Collection<StagedModel> _stagedModelCollection;
}
