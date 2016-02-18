package de.hpi.ir.yahoogle.query;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.bool.BooleanModel;
import de.hpi.ir.yahoogle.rm.bool.BooleanResult;

public class BooleanSearch extends Search<BooleanResult> {

	public BooleanSearch(Index index, String query) {
		super(index, query);
	}

	@Override
	public List<BooleanResult> search() {
		BooleanModel model = new BooleanModel(index);
		if (topK != ALL_RESULTS) {
			model.setTopK(topK);
		}
		Collection<BooleanResult> booleanResult = model.compute(query);
		return booleanResult.stream().sorted().limit(topK)
				.collect(Collectors.toList());
	}
}
