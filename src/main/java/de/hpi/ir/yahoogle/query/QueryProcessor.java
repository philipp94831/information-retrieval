package de.hpi.ir.yahoogle.query;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.hpi.ir.yahoogle.StopWordList;

public class QueryProcessor {
	
	private static final String PHRASE_DELIMITER = "\"";
	
	public static List<String> generateQueryPlan(String query) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		List<String> queryPlan = new ArrayList<>();
		StringBuilder phrase = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			boolean checkEmpty = false;
			switch (token.toLowerCase()) {
			case "and":
			case "or":
				checkEmpty = true;
			case "not":
				if (phrase.length() > 0) {
					queryPlan.add(phrase.toString().trim());
					phrase = new StringBuilder();
				} else {
					if (!queryPlan.isEmpty()) {
						queryPlan.remove(queryPlan.size() - 1);
					}
				}
				if (!(checkEmpty && queryPlan.isEmpty())) {
					queryPlan.add(token.trim());
				}
				break;
			default:
				String cleanedToken = token.replaceAll(PHRASE_DELIMITER, "");
				if (!StopWordList.isStopword(cleanedToken)) {
					phrase.append(" ").append(token);
				}
				break;
			}
		}
		if (phrase.length() > 0) {
			queryPlan.add(phrase.toString().trim());
		} else {
			if (!queryPlan.isEmpty()) {
				queryPlan.remove(queryPlan.size() - 1);
			}
		}
		return queryPlan;
	}

	public static List<String> extractPhrases(String partialQuery) {
		List<String> phrases = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(partialQuery);
		StringBuilder buffer = new StringBuilder();
		boolean inPhrase = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith(PHRASE_DELIMITER)) {
				inPhrase = true;
			}
			if (token.endsWith(PHRASE_DELIMITER)) {
				inPhrase = false;
			}
			buffer.append(" ").append(token.replaceAll(PHRASE_DELIMITER, ""));
			if (!inPhrase) {
				phrases.add(buffer.toString().trim());
				buffer = new StringBuilder();
			}
		}
		return phrases;
	}
}
