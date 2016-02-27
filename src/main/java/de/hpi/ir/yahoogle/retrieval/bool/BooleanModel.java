package de.hpi.ir.yahoogle.retrieval.bool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MinMaxPriorityQueue;

import de.hpi.ir.yahoogle.index.DocumentPosting;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.search.PhraseResultIterator;
import de.hpi.ir.yahoogle.retrieval.Model;
import de.hpi.ir.yahoogle.search.QueryProcessor;

public class BooleanModel extends Model<BooleanResult> {

	private static final String LINK_KEYWORD = "linkto:";

	public BooleanModel(Index index) {
		super(index);
	}

	@Override
	public Collection<BooleanResult> compute(String query) {
		Set<Integer> booleanResult = new HashSet<>();
		Operator operator = Operator.OR;
		List<String> queryPlan = QueryProcessor.generateQueryPlan(query);
		if (!queryPlan.isEmpty() && queryPlan.get(0).equalsIgnoreCase("not")) {
			booleanResult.addAll(index.getAllDocNumbers());
		}
		List<String> phrases = new ArrayList<>();
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
					result = findToken(newPhrases);
					if (operator != Operator.NOT) {
						phrases.addAll(newPhrases);
					}
				}
				switch (operator) {
				case AND:
					booleanResult.retainAll(result);
					break;
				case OR:
					booleanResult.addAll(result);
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
		results = booleanResult.size();
		MinMaxPriorityQueue<BooleanResult> cropped = MinMaxPriorityQueue
				.maximumSize(topK).create();
		booleanResult.forEach(i -> cropped.add(new BooleanResult(i)));
		Map<Integer, BooleanResult> result = new HashMap<>();
		cropped.forEach(b -> result.put(b.getDocNumber(), b));
		for (String phrase : phrases) {
			PhraseResultIterator iterator = index.findPositions(phrase);
			while (iterator.hasNext()) {
				DocumentPosting next = iterator.next();
				if (result.containsKey(next.getDocNumber())) {
					BooleanResult br = new BooleanResult(next.getDocNumber());
					br.addPositions(phrase, next.getAll());
					result.merge(next.getDocNumber(), br, BooleanResult::merge);
				}
			}
		}
		return result.values();
	}

	private static boolean isLinkPhrase(List<String> newPhrases) {
		return newPhrases.stream()
				.anyMatch(s -> s.toLowerCase().startsWith(LINK_KEYWORD));
	}

	private Set<Integer> findToken(List<String> phrases) {
		return index.find(phrases);
	}

	private Set<Integer> findLinks(List<String> phrases) {
		phrases = phrases.stream()
				.map(p -> p.toLowerCase().replaceAll(LINK_KEYWORD, ""))
				.collect(Collectors.toList());
		return index.findLinks(phrases);
	}
}
