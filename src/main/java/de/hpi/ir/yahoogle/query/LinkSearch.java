package de.hpi.ir.yahoogle.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.bool.BooleanLinkModel;
import de.hpi.ir.yahoogle.rm.bool.BooleanResult;

public class LinkSearch extends Search {

	public LinkSearch(Index index, String query) {
		super(index, query);
	}

	@Override
	public ArrayList<String> search() {
		query = query.replaceAll("LinkTo:", "");
		BooleanLinkModel model = new BooleanLinkModel(index);
		Set<BooleanResult> booleanResult = model.compute(query);
		List<BooleanResult> result = booleanResult.stream().limit(topK).collect(Collectors.toList());
		return generateSlimOutput(result);
	}
}
