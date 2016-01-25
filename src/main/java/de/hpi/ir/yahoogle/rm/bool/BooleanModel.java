package de.hpi.ir.yahoogle.rm.bool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.rm.Model;

public abstract class BooleanModel extends Model<BooleanResult> {

	public BooleanModel(Index index) {
		super(index);
	}

	@Override
	public Set<BooleanResult> compute(String query) {
		Set<Integer> booleanResult = new HashSet<>();
		Operator operator = Operator.OR;
		List<String> queryPlan = QueryProcessor.generateQueryPlan(query);
		if (queryPlan.get(0).equalsIgnoreCase("not")) {
			booleanResult.addAll(index.getAllDocNumbers());
		}
		phrases = new ArrayList<>();
		for (String phrase : queryPlan) {
			switch (phrase.toLowerCase()) {
			case "and":
				operator = Operator.AND;
				break;
			case "or":
				operator = Operator.OR;
				break;
			case "not":
				operator = Operator.NOT;
				break;
			default:
				List<String> newPhrases = QueryProcessor.extractPhrases(phrase);
				Set<Integer> result = find(newPhrases);
				switch (operator) {
				case AND:
					booleanResult.retainAll(result);
					phrases.addAll(newPhrases);
					break;
				case OR:
					booleanResult.addAll(result);
					phrases.addAll(newPhrases);
					break;
				case NOT:
					booleanResult.removeAll(result);
					break;
				default:
					break;
				}
				break;
			}
		}
		return booleanResult.stream().map(BooleanResult::new).collect(Collectors.toSet());
	}

	protected abstract Set<Integer> find(List<String> phrases);
}
