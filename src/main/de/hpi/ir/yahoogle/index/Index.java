package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.StopWordList;
import de.hpi.ir.yahoogle.ValueComparator;
import de.hpi.ir.yahoogle.YahoogleUtils;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;
import de.hpi.ir.yahoogle.rm.Model;
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
		Map<String, Integer> wordFrequencies = new HashMap<String, Integer>();
		int i = 1;
		while(tokenizer.hasMoreTokens()) {
			String token = YahoogleUtils.sanitize(tokenizer.nextToken());
			if (StopWordList.isStopword(token)) {
				continue;
			}
			i++;
			Integer freq = wordFrequencies.get(token);
			if(freq == null) {
				freq = 0;
			}
			freq++;
			wordFrequencies.put(token, freq);
			IndexPosting posting = new IndexPosting();
			posting.setPosition(i);
			indexBuffer.buffer(token, patent.getDocNumber(), posting);
		}
		int wordCount = i - 1;
		resume.setWordCount(wordCount);
		TreeMap<String, Integer> sortedResults = sortByValueDescending(wordFrequencies);
		HashMap<String, Double> strippedMap = new HashMap<String, Double>();
		Iterator<Entry<String, Integer>> iterator = sortedResults.entrySet().iterator();
		for(int j = 0; j < 10 && iterator.hasNext(); j++) {
			Entry<String, Integer> entry = iterator.next();
			strippedMap.put(entry.getKey(), (entry.getValue()/(double) wordCount));
		}
		resume.setWordFrequencies(strippedMap);
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
		while (tokenizer.hasMoreTokens()) {
			matchNextPhraseToken(result, findAll(tokenizer.nextToken()));
		}
		return result;
	}

	private void matchNextPhraseToken(Map<Integer, Set<Integer>> result, Map<Integer, Set<Integer>> nextResult) {
		for (Entry<Integer, Set<Integer>> entry : result.entrySet()) {
			Set<Integer> newPos = nextResult.get(entry.getKey());
			if (newPos != null) {
				Set<Integer> possibilities = entry.getValue().stream().map(p -> p + 1).collect(Collectors.toSet());
				possibilities.retainAll(newPos);
				result.put(entry.getKey(), possibilities);
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

	public List<Integer> findRelevant(List<String> phrases, int topK) {
		Model model = new QLModel(this);
		Map<Integer, Double> results = model.compute(phrases);
		TreeMap<Integer, Double> sortedResults = sortByValueDescending(results);
		List<Integer> result = new ArrayList<Integer>(sortedResults.keySet());
		return result.subList(0, Math.min(topK, result.size()));
	}

	public int wordCount() {
		return patents.getTotalWordCount();
	}

	public int wordCount(Integer docNumber) {
		return patents.wordCount(docNumber);
	}

	public List<String> getTopWords(int topK, List<Integer> results) {
		List<Map<String, Double>> maps = results.stream().map(d -> patents.get(d).getWordFrequencies()).collect(Collectors.toList());
		Map<String, Double> result = new HashMap<String, Double>();
		for(Map<String, Double> map : maps) {
			for(Entry<String, Double> entry : map.entrySet()) {
				Double count = result.get(entry.getKey());
				if(count == null) {
					count = 0.0;
				}
				count += entry.getValue();
				result.put(entry.getKey(), count);
			}
		}
		TreeMap<String, Double> sortedResults = sortByValueDescending(result);
		List<String> topWords = new ArrayList<String>(sortedResults.keySet());
		return topWords.subList(0, Math.min(topK, topWords.size()));
	}

	private <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<K, V>(result);
		TreeMap<K, V> sortedResults =  new TreeMap<K, V>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}

}
