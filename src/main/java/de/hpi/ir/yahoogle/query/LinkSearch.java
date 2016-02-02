package de.hpi.ir.yahoogle.query;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.bool.BooleanLinkModel;
import de.hpi.ir.yahoogle.rm.bool.BooleanResult;

public class LinkSearch extends Search<BooleanResult> {

	public LinkSearch(Index index, String query) {
		super(index, query);
	}

	@Override
	public List<BooleanResult> search() {
		BooleanLinkModel model = new BooleanLinkModel(index);
		Set<BooleanResult> booleanResult = model.compute(query);
		return booleanResult.stream().limit(topK).collect(Collectors.toList());
	}
}
