package de.hpi.ir.yahoogle.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.language.Stemmer;
import de.hpi.ir.yahoogle.language.Tokenizer;
import de.hpi.ir.yahoogle.rm.ql.QLModel;
import de.hpi.ir.yahoogle.rm.ql.QLResult;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;
import de.hpi.ir.yahoogle.util.ValueComparator;

public class RelevantSearch extends Search<QLResult> {

	private static final String PRF_SEPARATOR = "#";
	private static final int TOP_WORDS = 2;

	private static List<String> getTopWords(int topK, Collection<String> snippets) {
		Map<String, Integer> topwords = new HashMap<>();
		for (String snippet : snippets) {
			Tokenizer tokenizer = new Tokenizer(snippet);
			while (tokenizer.hasNext()) {
				String token = Stemmer.stem(tokenizer.next());
				Integer count = topwords.getOrDefault(token, 0);
				count++;
				topwords.put(token, count);
			}
		}
		TreeMap<String, Integer> sortedWords = ValueComparator
				.sortByValueDescending(topwords);
		List<String> topWords = new ArrayList<>(sortedWords.keySet());
		return topWords.subList(0, Math.min(topK, topWords.size()));
	}

	private int prf = 0;

	public RelevantSearch(Index index, String query) {
		super(index, query);
		this.prf = parsePrf();
	}

	private int parsePrf() {
		String[] parts = query.split(PRF_SEPARATOR);
		int prf = 0;
		if (parts.length > 1) {
			query = parts[0];
			prf = Integer.parseInt(parts[1].trim());
		}
		return prf;
	}

	@Override
	public SearchResult search() {
		SearchResult results = searchResults(Math.max(topK, prf));
		phrases = QueryProcessor.extractPhrases(query);
		Map<Integer, String> snippets = new SnippetGenerator(index)
				.generateSnippets(results.getResults(), phrases);
		if (prf > 0) {
			List<String> topWords = getTopWords(TOP_WORDS, snippets.values());
			List<String> newPhrases = new ArrayList<>();
			newPhrases.addAll(phrases);
			newPhrases.addAll(topWords);
			RelevantSearch rs = new RelevantSearch(index,
					String.join(" ", newPhrases));
			results = rs.searchResults(topK);
		}
		return results;
	}

	private SearchResult searchResults(int topK) {
		QLModel model = new QLModel(index);
		if (topK != ALL_RESULTS) {
			model.setTopK(topK);
		}
		return search(model);
	}
}
