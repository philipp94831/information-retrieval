package de.hpi.ir.yahoogle.query;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.hpi.ir.yahoogle.language.StopWordList;

public class QueryProcessor {

	private static final String PHRASE_DELIMITER = "\"";

	public static List<String> generateQueryPlan(String query) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		List<String> queryPlan = new ArrayList<>();
		List<String> phrase = new ArrayList<>();
		boolean inPhrase = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			boolean checkEmpty = false;
			String cleaned = token.toLowerCase().replaceAll(PHRASE_DELIMITER, "");;
			if (token.startsWith(PHRASE_DELIMITER)) {
				inPhrase = true;
			}
			if (cleaned.equals("or")) {
				checkEmpty = true;
			}
			if (!inPhrase && (cleaned.equals("and") || cleaned.equals("or") || cleaned.equals("not"))) {
				if (phrase.size() > 0) {
					queryPlan.add(String.join(" ", phrase));
					phrase = new ArrayList<>();
				} else {
					if (!queryPlan.isEmpty()) {
						queryPlan.remove(queryPlan.size() - 1);
					}
				}
				if (!(checkEmpty && queryPlan.isEmpty())) {
					queryPlan.add(token.trim());
				}
			} else {
				String cleanedToken = token.replaceAll(PHRASE_DELIMITER, "");
				if (!StopWordList.isStopword(cleanedToken)) {
					phrase.add(token);
				}
			}
			if (token.endsWith(PHRASE_DELIMITER)) {
				inPhrase = false;
			}
		}
		if (phrase.size() > 0) {
			queryPlan.add(String.join(" ", phrase));
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
			String cleanedToken = token.replaceAll(PHRASE_DELIMITER, "");
			if (!StopWordList.isStopword(cleanedToken)) {
				buffer.append(" ").append(cleanedToken);
			}
			if (!inPhrase && buffer.length() > 0) {
				phrases.add(buffer.toString().trim());
				buffer = new StringBuilder();
			}
		}
		return phrases;
	}

	public static QueryType getQueryType(String query) {
		if (isLinkQuery(query)) {
			return QueryType.LINK;
		} else if (isBooleanQuery(query)) {
			return QueryType.BOOLEAN;
		} else {
			return QueryType.RELEVANT;
		}
	}

	private static boolean isBooleanQuery(String query) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		boolean inPhrase = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String cleaned = token.toLowerCase().replaceAll(PHRASE_DELIMITER, "");
			if (token.startsWith(PHRASE_DELIMITER)) {
				inPhrase = true;
			}
			if (!inPhrase && (cleaned.equals("and") || cleaned.equals("or") || cleaned.equals("not"))) {
				return true;
			}
			if (token.endsWith(PHRASE_DELIMITER)) {
				inPhrase = false;
			}
		}
		return false;
	}

	private static boolean isLinkQuery(String query) {
		return query.toLowerCase().startsWith("linkto:");
	}
}
