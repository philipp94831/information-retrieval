package de.hpi.ir.yahoogle.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.retrieval.Model;
import de.hpi.ir.yahoogle.retrieval.Result;

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

	public abstract SearchResult search();

	protected SearchResult search(Model<T> model) {
		Collection<T> results = model.compute(query);
		Stream<T> stream = results.stream().sorted();
		if (topK != ALL_RESULTS) {
			stream = stream.limit(topK);
		}
		results = stream.collect(Collectors.toList());
		SearchResult result = new SearchResult();
		result.setResults(results);
		result.setResultSize(model.getResults());
		return result;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
