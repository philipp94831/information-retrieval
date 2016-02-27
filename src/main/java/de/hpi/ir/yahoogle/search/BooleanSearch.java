package de.hpi.ir.yahoogle.search;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.retrieval.bool.BooleanModel;
import de.hpi.ir.yahoogle.retrieval.bool.BooleanResult;

public class BooleanSearch extends Search<BooleanResult> {

	public BooleanSearch(Index index, String query) {
		super(index, query);
	}

	@Override
	public SearchResult search() {
		BooleanModel model = new BooleanModel(index);
		if (topK != ALL_RESULTS) {
			model.setTopK(topK);
		}
		return search(model);
	}
}
