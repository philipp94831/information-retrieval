package de.hpi.ir.yahoogle.search;

import java.util.Collection;

import de.hpi.ir.yahoogle.retrieval.Result;

public class SearchResult {

	private Collection<? extends Result> results;
	private int resultSize;

	public Collection<? extends Result> getResults() {
		return results;
	}

	public int getResultSize() {
		return resultSize;
	}

	public void setResults(Collection<? extends Result> results) {
		this.results = results;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}
}
