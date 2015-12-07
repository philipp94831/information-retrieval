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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.ModelResult;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

	private static final String PHRASE_DELIMITER = "\"";
	
	private Index index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex(String directory) {
		index(directory);
	}
	
	public static String getTeamDirectory() {
		return teamDirectory;
	}

	@Override
	void index(String directory) {

		try {
			index = new Index(directory);
			index.create();
			PatentParser handler = new PatentParser(index);
			
			File patents = new File(directory);

		    for(File patentFile : patents.listFiles()){
		        System.out.println(patentFile.getName());
		        FileInputStream stream = new FileInputStream(patentFile);
		        handler.setFileName(patentFile.getName());
		        handler.parse(stream);
		    }

			index.finish();
			index.write();

		} catch (FileNotFoundException e) {
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
		return index.load();
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		List<String> queryPlan = processQuery(query);
		if(queryPlan.isEmpty()) {
			return new ArrayList<String>();
		}
		if(queryPlan.size() == 1 && !queryPlan.get(0).contains("*")) {
			return searchRelevant(topK, prf, queryPlan);
		}
		return searchBoolean(queryPlan);
	}

	private ArrayList<String> searchBoolean(List<String> queryPlan) {
		Set<Integer> docNumbers = new HashSet<Integer>();
		Operator operator = Operator.OR;
		if(queryPlan.get(0).toLowerCase().equals("not")) {
			docNumbers = index.getAllDocNumbers();
		}
		for (String phrase : queryPlan) {
			switch(phrase.toLowerCase()) {
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
				Set<Integer> result = new HashSet<Integer>();
				for (int i = 0; i < phrases.size(); i++) {
					result.addAll(index.find(phrases.get(i)));
				}
				switch (operator) {
				case AND:
					docNumbers.retainAll(result);
					break;
				case OR:
					docNumbers.addAll(result);
					break;
				case NOT:
					docNumbers.removeAll(result);
					break;
				}
				break;
			}
		}
		return index.matchInventionTitles(docNumbers);
	}

	private ArrayList<String> searchRelevant(int topK, int prf, List<String> queryPlan) {
		List<String> phrases = extractPhrases(queryPlan.get(0));
		List<ModelResult> results = index.findRelevant(phrases, Math.max(topK, prf));
		Map<Integer, String> snippets = generateSnippets(results, phrases);
		if(prf > 0) {
			List<String> topWords = getTopWords(10, snippets.values());
			List<String> newPhrases = new ArrayList<String>();
			newPhrases.addAll(phrases);
			newPhrases.addAll(topWords);
			results = index.findRelevant(newPhrases, topK);
			snippets = generateSnippets(results, phrases);
		}
		return generateOutput(results, snippets);
	}

	private static List<String> extractPhrases(String partialQuery) {
		List<String> phrases = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(partialQuery);
		StringBuffer buffer = new StringBuffer();
		boolean inPhrase = false;
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(token.startsWith(PHRASE_DELIMITER)) {
				inPhrase = true;
			}
			if(token.endsWith(PHRASE_DELIMITER)) {
				inPhrase = false;
			}
			buffer.append(" " + token.replaceAll(PHRASE_DELIMITER, ""));
			if(!inPhrase) {
				phrases.add(buffer.toString());
				buffer = new StringBuffer();
			}
		}
		return phrases;
	}

	private static List<String> processQuery(String query) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		List<String> queryPlan = new ArrayList<String>();
		StringBuffer phrase = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			boolean checkEmpty = false;
			switch(token.toLowerCase()) {
			case "and":
			case "or":
				checkEmpty = true;
			case "not":
				if(phrase.length() > 0) {
					queryPlan.add(phrase.toString());
					phrase = new StringBuffer();
				} else {
					if(queryPlan.size() > 0) {
						queryPlan.remove(queryPlan.size() - 1);
					}
				}
				if(!(checkEmpty && queryPlan.isEmpty())) {
					queryPlan.add(token);
				}
				break;
			default:
				String cleanedToken = token.replaceAll(PHRASE_DELIMITER, "");
				if(!StopWordList.isStopword(cleanedToken)) {
					phrase.append(" " + token);
				}
				break;
			}
		}
		if(phrase.length() > 0) {
			queryPlan.add(phrase.toString());
		} else {
			if(queryPlan.size() > 0) {
				queryPlan.remove(queryPlan.size() - 1);
			}
		}
		return queryPlan;
	}

	public static List<String> getTopWords(int topK, Collection<String> collection) {
		Map<String, Integer> topwords = new HashMap<String, Integer>();
		for(String snippet : collection) {
			StringTokenizer tokenizer = new StringTokenizer(snippet);
			while(tokenizer.hasMoreTokens()) {
				String token = Stemmer.stem(tokenizer.nextToken());
				if (StopWordList.isStopword(token)) {
					continue;
				}
				Integer count = topwords.getOrDefault(token, 0);
				count++;
				topwords.put(token, count);
			}
		}
		TreeMap<String, Integer> sortedWords = sortByValueDescending(topwords);
		List<String> topWords = new ArrayList<String>(sortedWords.keySet());
		return topWords.subList(0, Math.min(topK, topWords.size()));
	}

	private static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<K, V>(result);
		TreeMap<K, V> sortedResults =  new TreeMap<K, V>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}

	public Map<Integer, String> generateSnippets(List<? extends Result> results, List<String> phrases) {
		SnippetGenerator generator = new SnippetGenerator(phrases);
		Map<Integer, String> snippets = new HashMap<Integer, String>();
		for(Result result : results) {
			int docNumber = result.getDocNumber();
			String snippet = generator.generate(result, index.getPatent(docNumber).getPatent().getPatentAbstract());
			snippets.put(docNumber, snippet);
		}
		return snippets;
	}
	
	public ArrayList<String> generateOutput(List<ModelResult> results2, Map<Integer, String> snippets) {
		ArrayList<String> results = new ArrayList<String>();
		for (ModelResult result : results2) {
			int docNumber = result.getDocNumber();
			results.add(String.format("%08d", docNumber) + "\t" + index.getPatent(docNumber).getPatent().getInventionTitle() + "\n" + snippets.get(docNumber));
		}
		return results;
	}

}
