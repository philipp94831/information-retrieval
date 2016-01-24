package de.hpi.ir.yahoogle.rm.bool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.Operator;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.rm.Model;

public class BooleanModel extends Model<BooleanResult> {

	private ArrayList<String> allPhrases;

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
		allPhrases = new ArrayList<>();
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
				List<String> phrases = QueryProcessor.extractPhrases(phrase);
				Set<Integer> result = find(phrases);
				switch (operator) {
				case AND:
					booleanResult.retainAll(result);
					allPhrases.addAll(phrases);
					break;
				case OR:
					booleanResult.addAll(result);
					allPhrases.addAll(phrases);
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

	protected Set<Integer> find(List<String> phrases) {
		return index.find(phrases);
	}

	public List<String> getPhrases() {
		return allPhrases;
	}
}
