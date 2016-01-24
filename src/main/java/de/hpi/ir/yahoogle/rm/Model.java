package de.hpi.ir.yahoogle.rm;

import java.util.List;

import de.hpi.ir.yahoogle.index.Index;

public abstract class Model<T extends Result> {

	protected final Index index;
	protected int topK = Integer.MAX_VALUE;

	protected Model(Index index) {
		this.index = index;
	}

	public abstract List<T> compute(List<String> query);

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
