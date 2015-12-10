package de.hpi.ir.yahoogle.rm;

import java.util.List;

import de.hpi.ir.yahoogle.index.Index;

public abstract class Model {

	final Index index;

	Model(Index index) {
		this.index = index;
	}

	public abstract List<ModelResult> compute(List<String> query);
}
