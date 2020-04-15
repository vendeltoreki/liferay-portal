package com.liferay.exportimport.changeset;

import java.io.Serializable;
import java.util.function.Supplier;

import com.liferay.portal.kernel.model.StagedModel;

public class StagedModelSupplier implements Supplier<StagedModel>, Serializable {

	@Override
	public StagedModel get() {
		return _stagedModel;
	}

	public StagedModelSupplier(StagedModel stagedModel) {
		super();
		this._stagedModel = stagedModel;
	}

	private StagedModel _stagedModel;
}
