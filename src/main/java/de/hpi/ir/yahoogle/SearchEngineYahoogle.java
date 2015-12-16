package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author: Yahoogle
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * This is your file! implement your search engine here!
 * 
 * Describe your search engine briefly:
 *  - multi-threaded?
 *  - stemming?
 *  - stopword removal?
 *  - index algorithm?
 *  - etc.  
 * 
 * Keep in mind to include your implementation decisions also in the pdf file of each assignment
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PartialIndex;
import de.hpi.ir.yahoogle.index.partial.PartialIndexFactory;
import de.hpi.ir.yahoogle.parsing.PatentParser;
import de.hpi.ir.yahoogle.rm.ModelResult;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template'
															// with your search
															// engine's name,
															// i.e.
															// SearchEngineMyTeamName

	private static final String PHRASE_DELIMITER = "\"";
	private static final int TOP_WORDS = 4;

	private static List<String> extractPhrases(String partialQuery) {
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
				phrases.add(buffer.toString());
				buffer = new StringBuilder();
			}
		}
		return phrases;
	}

	public static String getTeamDirectory() {
		return teamDirectory;
	}

	private static List<String> getTopWords(int topK, Collection<String> collection) {
		Map<String, Integer> topwords = new HashMap<>();
		for (String snippet : collection) {
			Tokenizer tokenizer = new Tokenizer(snippet);
			while (tokenizer.hasNext()) {
				String token = Stemmer.stem(tokenizer.next());
				Integer count = topwords.getOrDefault(token, 0);
				count++;
				topwords.put(token, count);
			}
		}
		TreeMap<String, Integer> sortedWords = sortByValueDescending(topwords);
		List<String> topWords = new ArrayList<>(sortedWords.keySet());
		return topWords.subList(0, Math.min(topK, topWords.size()));
	}

	private static List<String> processQuery(String query) {
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
					queryPlan.add(phrase.toString());
					phrase = new StringBuilder();
				} else {
					if (!queryPlan.isEmpty()) {
						queryPlan.remove(queryPlan.size() - 1);
					}
				}
				if (!(checkEmpty && queryPlan.isEmpty())) {
					queryPlan.add(token);
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
			queryPlan.add(phrase.toString());
		} else {
			if (!queryPlan.isEmpty()) {
				queryPlan.remove(queryPlan.size() - 1);
			}
		}
		return queryPlan;
	}

	private static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<>(result);
		TreeMap<K, V> sortedResults = new TreeMap<>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}

	private Index index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex(String directory) {
		index(directory);
	}

	private ArrayList<String> generateOutput(Collection<? extends Result> results2, Map<Integer, String> snippets) {
		ArrayList<String> results = new ArrayList<>();
		for (Result result : results2) {
			int docNumber = result.getDocNumber();
			results.add(String.format("%08d", docNumber) + "\t"
					+ index.getPatent(docNumber).getPatent().getInventionTitle()
					+ "\n" + snippets.get(docNumber));
		}
		return results;
	}

	private Map<Integer, String> generateSnippets(Collection<? extends Result> results, List<String> phrases) {
		SnippetGenerator generator = new SnippetGenerator(phrases);
		Map<Integer, String> snippets = new HashMap<>();
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			String snippet = generator.generate(result,
					index.getPatent(docNumber));
			snippets.put(docNumber, snippet);
		}
		return snippets;
	}

	@Override
	void index(String directory) {
		try {
			PartialIndexFactory factory = new PartialIndexFactory();
			File patents = new File(directory);
			for (File patentFile : patents.listFiles()) {
				PartialIndex partialIndex = factory.getPartialIndex();
				partialIndex.create();
				PatentParser handler = new PatentParser(partialIndex);
				System.out.println(patentFile.getName());
				FileInputStream stream = new FileInputStream(patentFile);
				handler.setFileName(patentFile.getName());
				handler.parse(stream);
				partialIndex.write();
			}
			index = new Index(directory);
			index.create();
			index.mergeIndices(factory.getNames());
			index.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		return loadIndex(directory);
	}

	@Override
	boolean loadIndex(String directory) {
		index = new Index(directory);
		try {
			index.load();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		List<String> queryPlan = processQuery(query);
		if (queryPlan.isEmpty()) {
			return new ArrayList<>();
		}
		if (queryPlan.size() == 1) {
			return searchRelevant(topK, prf, queryPlan);
		}
		return searchBoolean(topK, queryPlan);
	}

	private ArrayList<String> searchBoolean(int topK, List<String> queryPlan) {
		Map<Integer, Result> docNumbers = new HashMap<>();
		Operator operator = Operator.OR;
		if (queryPlan.get(0).equalsIgnoreCase("not")) {
			docNumbers.putAll(index.getAllDocNumbers().stream()
					.collect(Collectors.toMap(d -> d, Result::new)));
		}
		List<String> allPhrases = new ArrayList<>();
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
				List<String> phrases = extractPhrases(phrase);
				Map<Integer, Result> result = new HashMap<>();
				for (String p : phrases) {
					index.find(p).forEach(
							r -> result.merge(r.getDocNumber(), r, (v1, v2) -> {
								v1.merge(v2);
								return v1;
							}));
				}
				switch (operator) {
				case AND:
					docNumbers.keySet().retainAll(result.keySet());
					result.keySet().retainAll(docNumbers.keySet());
				case OR:
					result.values().forEach(r -> docNumbers
							.merge(r.getDocNumber(), r, (r1, r2) -> {
								r1.merge(r2);
								return r1;
							}));
					allPhrases.addAll(phrases);
					break;
				case NOT:
					docNumbers.keySet().removeAll(result.keySet());
					break;
				default:
					break;
				}
				break;
			}
		}
		List<Result> r = docNumbers.values().stream().limit(topK)
				.collect(Collectors.toList());
		Map<Integer, String> snippets = generateSnippets(r, allPhrases);
		return generateOutput(r, snippets);
	}

	private ArrayList<String> searchRelevant(int topK, int prf, List<String> queryPlan) {
		List<String> phrases = extractPhrases(queryPlan.get(0));
		List<ModelResult> results = index.findRelevant(phrases,
				Math.max(topK, prf));
		Map<Integer, String> snippets = generateSnippets(results, phrases);
		if (prf > 0) {
			List<String> topWords = getTopWords(TOP_WORDS, snippets.values());
			List<String> newPhrases = new ArrayList<>();
			newPhrases.addAll(phrases);
			newPhrases.addAll(topWords);
			results = index.findRelevant(newPhrases, topK);
			snippets = generateSnippets(results, phrases);
		}
		return generateOutput(results, snippets);
	}

	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		// TODO Auto-generated method stub
		return null;
	}
}
