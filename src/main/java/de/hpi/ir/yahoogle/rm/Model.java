package de.hpi.ir.yahoogle.rm;

import java.util.List;

import de.hpi.ir.yahoogle.index.Index;

public abstract class Model {

	protected final Index index;

	public Model(Index index) {
		this.index = index;
	}

	public abstract List<ModelResult> compute(List<String> query);
}
