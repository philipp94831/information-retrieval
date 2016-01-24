package de.hpi.ir.yahoogle.rm;

import java.util.List;

import de.hpi.ir.yahoogle.index.Index;

abstract class Model {

	final Index index;
	int topK = Integer.MAX_VALUE;

	Model(Index index) {
		this.index = index;
	}

	public abstract List<? extends Result> compute(List<String> query);

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
