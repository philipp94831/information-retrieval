package de.hpi.ir.yahoogle.search;

import java.util.ArrayList;
import java.util.List;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.Result;

public abstract class Search<T extends Result> {

	protected static final int ALL_RESULTS = -1;
	protected final Index index;
	protected List<String> phrases = new ArrayList<>();
	protected String query;
	protected int topK = ALL_RESULTS;

	public Search(Index index, String query) {
		this.index = index;
		this.query = query;
	}

	public List<String> getPhrases() {
		return phrases;
	}

	public String getQuery() {
		return query;
	}

	public abstract List<T> search();

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
