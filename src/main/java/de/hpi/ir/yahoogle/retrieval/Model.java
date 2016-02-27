package de.hpi.ir.yahoogle.retrieval;

import java.util.Collection;
import de.hpi.ir.yahoogle.index.Index;

public abstract class Model<T extends Result> {

	protected final Index index;
	protected int results;
	protected int topK = Integer.MAX_VALUE / 1000;

	protected Model(Index index) {
		this.index = index;
	}

	public abstract Collection<T> compute(String query);

	public int getResults() {
		return results;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
