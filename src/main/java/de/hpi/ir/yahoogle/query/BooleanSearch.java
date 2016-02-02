package de.hpi.ir.yahoogle.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.rm.bool.BooleanModel;
import de.hpi.ir.yahoogle.rm.bool.BooleanTokenModel;
import de.hpi.ir.yahoogle.rm.ql.QLResult;

public class BooleanSearch extends Search<QLResult> {

	public BooleanSearch(Index index, String query) {
		super(index, query);
	}

	@Override
	public List<QLResult> search() {
		BooleanModel model = new BooleanTokenModel(index);
		Set<Integer> booleanResult = model.compute(query).stream().map(Result::getDocNumber)
				.collect(Collectors.toSet());
		Map<Integer, QLResult> result = new HashMap<>();
		RelevantSearch rs = new RelevantSearch(index, String.join(" ", model.getPhrases()));
		rs.searchResults(booleanResult).forEach(r -> result.put(r.getDocNumber(), r));
		phrases = model.getPhrases();
		return result.values().stream().sorted().limit(topK).collect(Collectors.toList());
	}
}
