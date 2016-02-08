package de.hpi.ir.yahoogle.rm.bool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.rm.Model;

public class BooleanModel extends Model<BooleanResult> {

	public BooleanModel(Index index) {
		super(index);
	}

	@Override
	public Set<BooleanResult> compute(String query) {
		Set<Integer> booleanResult = new HashSet<>();
		Operator operator = Operator.OR;
		List<String> queryPlan = QueryProcessor.generateQueryPlan(query);
		if (!queryPlan.isEmpty() && queryPlan.get(0).equalsIgnoreCase("not")) {
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
				Set<Integer> result;
				if (isLinkPhrase(newPhrases)) {
					result = findLinks(newPhrases);
				} else {
					phrases.addAll(newPhrases);
					result = findToken(newPhrases);
				}
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
		return booleanResult.stream().map(BooleanResult::new)
				.collect(Collectors.toSet());
	}

	private boolean isLinkPhrase(List<String> newPhrases) {
		return newPhrases.stream().anyMatch(s -> s.toLowerCase().startsWith("linkto:"));
	}

	private Set<Integer> findToken(List<String> phrases) {
		return index.find(phrases);
	}

	private Set<Integer> findLinks(List<String> phrases) {
		phrases = phrases.stream().map(p -> p.replaceAll("LinkTo:", ""))
				.collect(Collectors.toList());
		return index.findLinks(phrases);
	}
}
