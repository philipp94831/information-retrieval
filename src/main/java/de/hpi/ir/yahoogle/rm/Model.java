package de.hpi.ir.yahoogle.rm;

import java.util.Collection;
import de.hpi.ir.yahoogle.index.Index;

public abstract class Model<T extends Result> {

	protected final Index index;
	protected int topK = Integer.MAX_VALUE;

	public Model(Index index) {
		this.index = index;
	}

	public abstract Collection<T> compute(String query);

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
