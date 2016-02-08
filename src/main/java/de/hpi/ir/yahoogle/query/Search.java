package de.hpi.ir.yahoogle.query;

import java.util.ArrayList;
import java.util.List;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.Result;

public abstract class Search<T extends Result> {

	protected static final int ALL_RESULTS = -1;
	protected final Index index;
	protected String query;
	protected List<String> phrases = new ArrayList<>();
	protected int topK = ALL_RESULTS;

	public void setTopK(int topK) {
		this.topK = topK;
	}

	public Search(Index index, String query) {
		this.index = index;
		this.query = query;
	}

	public abstract List<T> search();

	public String getQuery() {
		return query;
	}

	public List<String> getPhrases() {
		return phrases;
	}
}
