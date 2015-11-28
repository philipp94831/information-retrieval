package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.StopWordList;
import de.hpi.ir.yahoogle.ValueComparator;
import de.hpi.ir.yahoogle.YahoogleUtils;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;
import de.hpi.ir.yahoogle.rm.Model;
import de.hpi.ir.yahoogle.rm.ModelResult;
import de.hpi.ir.yahoogle.rm.QLModel;

public class Index {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB
	private static final String PATENTS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/patents.yahoogle";

	private LinkedRandomAccessIndex linkedIndex;
	private OrganizedRandomAccessIndex organizedIndex;
	private PatentIndex patents;
	private IndexBuffer indexBuffer;

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	public void add(Patent patent) {
		PatentResume resume = new PatentResume(patent);		
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		int i = 1;
		while(tokenizer.hasMoreTokens()) {
			String token = YahoogleUtils.sanitize(tokenizer.nextToken());
			if (StopWordList.isStopword(token)) {
				continue;
			}
			IndexPosting posting = new IndexPosting();
			posting.setPosition(i);
			indexBuffer.buffer(token, patent.getDocNumber(), posting);
			i++;
		}
		int wordCount = i - 1;
		resume.setWordCount(wordCount);
		index(patent.getDocNumber(), resume);
		if (Runtime.getRuntime().freeMemory() < FLUSH_MEM_THRESHOLD) {
			flush();
		}
	}

	public boolean create() {
		boolean status = YahoogleUtils.deleteIfExists(PATENTS_FILE);
		linkedIndex = LinkedRandomAccessIndex.create();
		indexBuffer = new IndexBuffer(linkedIndex);
		patents = new PatentIndex();
		return status && (linkedIndex != null);
	}

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String phrase) {
		Map<Integer, Set<Integer>> result = findWithPositions(phrase);
		return getNotEmptyKeys(result);
	}

	public Map<Integer, Set<Integer>> findWithPositions(String phrase) {
		StringTokenizer tokenizer = new StringTokenizer(phrase);
		Map<Integer, Set<Integer>> result = null;
		if (tokenizer.hasMoreTokens()) {
			result = findAll(tokenizer.nextToken());
		}
		for (int i = 1; tokenizer.hasMoreTokens(); i++) {
			matchNextPhraseToken(result, findAll(tokenizer.nextToken()), i);
		}
		return result;
	}

	private void matchNextPhraseToken(Map<Integer, Set<Integer>> result, Map<Integer, Set<Integer>> nextResult, int delta) {
		for (Entry<Integer, Set<Integer>> entry : result.entrySet()) {
			Set<Integer> newPos = nextResult.get(entry.getKey());
			if (newPos != null) {
				Set<Integer> oldPos = entry.getValue();
				oldPos.retainAll(newPos.stream().map(p -> p - delta).collect(Collectors.toSet()));
				result.put(entry.getKey(), oldPos);
			} else {
				result.get(entry.getKey()).clear();
			}
		}
	}

	private Map<Integer, Set<Integer>> findAll(String token) {
		boolean prefix = token.endsWith("*");
		if (prefix) {
			String pre = token.substring(0, token.length() - 1);
			Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();
			for (String t : organizedIndex.getTokensForPrefix(pre)) {
				merge(result, organizedIndex.find(t));
			}
			return result;
		} else {
			return organizedIndex.find(YahoogleUtils.sanitize(token));
		}
	}

	/**
	 * flushes indexBuffer and reorganize temporary index to final index
	 */
	public void finish() {
		flush();
		organizedIndex = linkedIndex.reorganize();
//		printDictionary();
	}

	@SuppressWarnings("unused")
	private void printDictionary() {
		try {
			PrintWriter writer = new PrintWriter("dictionary.txt", "UTF-8");
			for(String token : organizedIndex.getTokens()) {
				writer.println(token);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void flush() {
		indexBuffer.flush();
	}

	public Set<Integer> getAllDocNumbers() {
		return patents.getAllDocNumbers();
	}

	private Set<Integer> getNotEmptyKeys(Map<Integer, Set<Integer>> result) {
		return result.entrySet().stream().filter(e -> e.getValue().size() > 0).map(e -> e.getKey()).collect(Collectors.toSet());
	}

	private void index(int docNumber, PatentResume resume) {
		patents.add(docNumber, resume);
	}

	/**
	 * loads index from disk
	 * 
	 * @return success value
	 */
	public boolean load() {
		try {
			organizedIndex = OrganizedRandomAccessIndex.load();
			patents = (PatentIndex) ObjectReader.readObject(PATENTS_FILE);
		} catch (FileNotFoundException e) {
			return false;
		}
		return (organizedIndex != null) && (patents != null);
	}

	/**
	 * matches given docNumbers to invention titles
	 * 
	 * @param docNumbers
	 *            a set of docNumbers
	 * @return list of invention titles
	 */
	public ArrayList<String> matchInventionTitles(Iterable<Integer> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Integer docNumber : docNumbers) {
			results.add(String.format("%08d", docNumber) + "\t" + patents.get(docNumber).getInventionTitle());
		}
		return results;
	}
	
	public ArrayList<String> generateOutput(List<ModelResult> results2, Map<Integer, String> snippets) {
		ArrayList<String> results = new ArrayList<String>();
		for (ModelResult result : results2) {
			int docNumber = result.getDocNumber();
			results.add(String.format("%08d", docNumber) + "\t" + patents.get(docNumber).getInventionTitle() + "\n" + snippets.get(docNumber));
		}
		return results;
	}

	private void merge(Map<Integer, Set<Integer>> result, Map<Integer, Set<Integer>> newResult) {
		for (Entry<Integer, Set<Integer>> entry : newResult.entrySet()) {
			Set<Integer> l = result.get(entry.getKey());
			if (l != null) {
				l.addAll(entry.getValue());
			} else {
				result.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public boolean write() {
		return organizedIndex.saveToDisk() && ObjectWriter.writeObject(patents, PATENTS_FILE);
	}

	public List<ModelResult> findRelevant(List<String> phrases, int topK) {
		Model model = new QLModel(this);
		List<ModelResult> results = model.compute(phrases);
		Collections.sort(results);
		return results.subList(0, Math.min(topK, results.size()));
	}

	public int wordCount() {
		return patents.getTotalWordCount();
	}

	public int wordCount(Integer docNumber) {
		return patents.wordCount(docNumber);
	}

	public List<String> getTopWords(int topK, Collection<String> collection) {
		Map<String, Integer> topwords = new HashMap<String, Integer>();
		for(String snippet : collection) {
			StringTokenizer tokenizer = new StringTokenizer(snippet);
			while(tokenizer.hasMoreTokens()) {
				String token = YahoogleUtils.sanitize(tokenizer.nextToken());
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

	private <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<K, V>(result);
		TreeMap<K, V> sortedResults =  new TreeMap<K, V>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}

	public Map<Integer, String> generateSnippets(List<ModelResult> results, List<String> phrases) {
		Map<Integer, String> snippets = new HashMap<Integer, String>();
		final int maxWindowLength = 10;
		for(ModelResult result : results) {
			int docNumber = result.getDocNumber();
			PatentResume resume = patents.get(docNumber);
			String patentAbstract = resume.getPatentAbstract();
			StringTokenizer tokenizer = new StringTokenizer(patentAbstract);
			int numberOfTokens = 0;
			while(tokenizer.hasMoreTokens()) {
				String token = YahoogleUtils.sanitize(tokenizer.nextToken());
				if (StopWordList.isStopword(token)) {
					continue;
				}
				numberOfTokens++;
			}
			int bestWindow = 0;
			int distinctMatchesInBestWindow = 0;
			int matchesInBestWindow = 0;
			for(int i = 1; i < Math.max(1, numberOfTokens - maxWindowLength); i++) {
				int distinctMatches = 0;
				int matchesInWindow = 0;
				for(String phrase : phrases) {
					TreeSet<Integer> positions = new TreeSet<Integer>(result.getPositions(phrase));
					int tokensInPhrase = new StringTokenizer(phrase).countTokens();
					int matches = positions.tailSet(i, true).headSet(i + maxWindowLength - tokensInPhrase + 1).size();
					if(matches > 0) {
						distinctMatches++;
					}
					matchesInWindow += matches;
				}
				if (distinctMatchesInBestWindow < distinctMatches || distinctMatchesInBestWindow == distinctMatches && matchesInBestWindow < matchesInWindow) {
					bestWindow = i;
					distinctMatchesInBestWindow = distinctMatches;
					matchesInBestWindow = matchesInWindow;
				}
			}
			tokenizer = new StringTokenizer(patentAbstract);
			int currentPosition = 1;
			StringBuilder snippetBuilder = new StringBuilder();
			while(tokenizer.hasMoreTokens() && currentPosition < bestWindow + maxWindowLength) {
				String token = tokenizer.nextToken();
				if(currentPosition >= bestWindow) {
					snippetBuilder.append(" " + token);
				}
				if (StopWordList.isStopword(token)) {
					continue;
				}
				currentPosition++;
			}
			String snippet = snippetBuilder.toString().trim();
			snippets.put(docNumber, snippet);
		}
		return snippets;
	}

}
