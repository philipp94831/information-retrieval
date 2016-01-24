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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PartialIndexFactory;
import de.hpi.ir.yahoogle.parsing.PatentParser;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.rm.ql.QLResult;
import de.hpi.ir.yahoogle.rm.ql.QLResultComparator;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template'
															// with your search
															// engine's name,
															// i.e.
															// SearchEngineMyTeamName

	private static final Logger LOGGER = Logger
			.getLogger(SearchEngineYahoogle.class.getName());
	private static final int TOP_WORDS = 4;

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
		TreeMap<String, Integer> sortedWords = ValueComparator
				.sortByValueDescending(topwords);
		List<String> topWords = new ArrayList<>(sortedWords.keySet());
		return topWords.subList(0, Math.min(topK, topWords.size()));
	}

	private Index index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex() {
		index();
	}

	private double computeGain(int goldRank) {
		return 1 + Math.floor(10 * Math.pow(0.5, 0.1 * goldRank));
	}

	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		double originalDcg = 0.0;
		double goldDcg = 0.0;
		for (int i = 0; i < p; i++) {
			String original = ranking.get(i);
			int goldRank = goldRanking.indexOf(original) + 1;
			double originalGain = computeGain(goldRank);
			double goldGain = computeGain(i + 1);
			if (goldRank == 0) {
				originalGain = 0;
			}
			if (i == 0) {
				originalDcg = originalGain;
				goldDcg = goldGain;
			} else {
				originalDcg += originalGain * Math.log(2) / Math.log(i + 1);
				goldDcg += goldGain * Math.log(2) / Math.log(i + 1);
			}
		}
		return originalDcg / goldDcg;
	}

	private ArrayList<String> generateOutput(Collection<? extends Result> results2, Map<Integer, String> snippets, String query) {
		ArrayList<String> results = new ArrayList<>();
		String googleQuery = toGoogleQuery(query);
		ArrayList<String> goldRanking = new WebFile()
				.getGoogleRanking(googleQuery);
		ArrayList<String> originalRanking = new ArrayList<>(
				results2.stream().map(r -> Integer.toString(r.getDocNumber()))
						.collect(Collectors.toList()));
		int i = 1;
		for (Result result : results2) {
			int docNumber = result.getDocNumber();
			double ndcg = computeNdcg(goldRanking, originalRanking, i);
			results.add(String.format("%08d", docNumber) + "\t"
					+ index.getPatent(docNumber).getPatent().getInventionTitle()
					+ "\t" + ndcg + "\n" + snippets.get(docNumber));
			i++;
		}
		return results;
	}

	private ArrayList<String> generateSlimOutput(List<Integer> r) {
		ArrayList<String> results = new ArrayList<>();
		for (Integer docNumber : r) {
			results.add(String.format("%08d", docNumber) + "\t" + index
					.getPatent(docNumber).getPatent().getInventionTitle());
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
	void index() {
		try {
			PartialIndexFactory factory = new PartialIndexFactory();
			File patents = new File(dataDirectory);
			factory.start();
			PatentParser handler = new PatentParser(factory);
			File[] files = patents.listFiles();
			for (File patentFile : files) {
				LOGGER.info(patentFile.getName());
				FileInputStream stream = new FileInputStream(patentFile);
				handler.setFileName(patentFile.getName());
				handler.parse(stream);
			}
			factory.finish();
			index = new Index(dataDirectory);
			index.create();
			index.mergeIndices(factory.getNames());
			index.write();
		} catch (IOException e) {
			LOGGER.severe("Error indexing files");
		} catch (XMLStreamException e) {
			LOGGER.severe("Error parsing XML");
		}
	}

	private boolean isBooleanQuery(List<String> queryPlan) {
		return queryPlan.size() == 1;
	}

	private boolean isLinkQuery(String query) {
		return query.startsWith("LinkTo:");
	}

	@Override
	boolean loadCompressedIndex() {
		return loadIndex();
	}

	@Override
	boolean loadIndex() {
		index = new Index(dataDirectory);
		try {
			index.load();
			index.warmUp();
			return true;
		} catch (IOException e) {
			LOGGER.severe("Error loading Index from disk");
		}
		return false;
	}

	private int parsePrf(String[] parts) {
		int prf = 0;
		if (parts.length > 1) {
			prf = Integer.parseInt(parts[1].trim());
		}
		return prf;
	}

	@Override
	ArrayList<String> search(String query, int topK) {
		String[] parts = query.split("#");
		query = parts[0];
		if (isLinkQuery(query)) {
			return searchLinks(topK, query);
		}
		List<String> queryPlan = QueryProcessor.generateQueryPlan(query);
		if (queryPlan.isEmpty()) {
			return new ArrayList<>();
		}
		if (isBooleanQuery(queryPlan)) {
			int prf = parsePrf(parts);
			return searchRelevant(topK, prf, query);
		}
		return searchBoolean(topK, queryPlan, query);
	}

	private ArrayList<String> searchBoolean(int topK, List<String> queryPlan, String query) {
		Set<Integer> booleanResult = new HashSet<>();
		Operator operator = Operator.OR;
		if (queryPlan.get(0).equalsIgnoreCase("not")) {
			booleanResult.addAll(index.getAllDocNumbers());
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
				List<String> phrases = QueryProcessor.extractPhrases(phrase);
				Set<Integer> result = index.find(phrases);
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
		Map<Integer, QLResult> result = new HashMap<>();
		index.findRelevant(allPhrases)
				.forEach(r -> result.put(r.getDocNumber(), r));
		result.keySet().retainAll(booleanResult);
		List<QLResult> r = result.values().stream()
				.sorted(new QLResultComparator()).limit(topK)
				.collect(Collectors.toList());
		Map<Integer, String> snippets = generateSnippets(r, allPhrases);
		return generateOutput(r, snippets, query);
	}

	private ArrayList<String> searchLinks(int topK, String query) {
		query = query.replaceAll("LinkTo:", "");
		List<String> queryPlan = QueryProcessor.generateQueryPlan(query);
		Set<Integer> results = new HashSet<>();
		Operator operator = Operator.OR;
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
				List<Integer> intermediate = index.findLinks(phrase);
				switch (operator) {
				case AND:
					results.retainAll(intermediate);
					break;
				case OR:
					results.addAll(intermediate);
					break;
				case NOT:
					results.removeAll(intermediate);
					break;
				default:
					break;
				}
				break;
			}
		}
		List<Integer> r = results.stream().limit(topK)
				.collect(Collectors.toList());
		return generateSlimOutput(r);
	}

	private ArrayList<String> searchRelevant(int topK, int prf, String query) {
		List<String> phrases = QueryProcessor.extractPhrases(query);
		List<QLResult> results = index.findRelevant(phrases,
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
		return generateOutput(results, snippets, query);
	}

	private String toGoogleQuery(String query) {
		return query.toLowerCase().replaceAll("\\snot\\s", " -")
				.replaceAll("^not\\s", "-");
	}
}
