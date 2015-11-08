package de.hpi.ir.yahoogle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class YahoogleIndex {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB
	private static final String PATENTS_FILE = SearchEngineYahoogle.teamDirectory + "/patents.yahoogle";

	private LinkedRandomAccessIndex tmp_index;
	private RandomAccessIndex index;
	private PatentIndex patents;
	private Map<String, YahoogleTokenMap> tokenMap;

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	public void add(Patent patent) {
		index(patent);
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for (short i = 1; tokenizer.hasMoreTokens();) {
			String token = YahoogleUtils.sanitize(tokenizer.nextToken());
			if (StopWordList.isStopword(token)) {
				continue;
			}
			i++;
			YahoogleIndexPosting posting = new YahoogleIndexPosting();
			posting.setPosition(i);
			buffer(token, patent.getDocNumber(), posting);
		}

		if (Runtime.getRuntime().freeMemory() < FLUSH_MEM_THRESHOLD) {
			flush();
		}
	}

	public void buffer(String token, int docNumber, YahoogleIndexPosting posting) {
		if (tokenMap.get(token) == null) {
			tokenMap.put(token, new YahoogleTokenMap());
		}
		tokenMap.get(token).add(docNumber, posting);
	}

	public boolean create() {
		boolean status = YahoogleUtils.deleteIfExists(PATENTS_FILE);
		tmp_index = LinkedRandomAccessIndex.create();
		tokenMap = new HashMap<String, YahoogleTokenMap>();
		patents = new PatentIndex();
		return status && (tmp_index != null);
	}

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String phrase) {
		StringTokenizer tokenizer = new StringTokenizer(phrase);
		Map<Integer, Set<Integer>> result = null;
		if (tokenizer.hasMoreTokens()) {
			result = findAll(tokenizer.nextToken());
		}
		while (tokenizer.hasMoreTokens()) {
			matchNextPhraseToken(result, findAll(tokenizer.nextToken()));
		}
		return getNotEmptyKeys(result);
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
			for (String t : index.getTokensForPrefix(pre)) {
				merge(result, primitiveFind(t));
			}
			return result;
		} else {
			return primitiveFind(YahoogleUtils.sanitize(token));
		}
	}

	/**
	 * flushes indexBuffer and reorganize temporary index to final index
	 */
	public void finish() {
		flush();
		index = tmp_index.reorganize();
	}

	public void flush() {
		try {
			for (Entry<String, YahoogleTokenMap> entry : tokenMap.entrySet()) {
				tmp_index.add(entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokenMap.clear();
	}

	public Set<Integer> getAllDocNumbers() {
		return patents.getAllDocNumbers();
	}

	private Set<Integer> getNotEmptyKeys(Map<Integer, Set<Integer>> result) {
		return result.entrySet().stream().filter(e -> e.getValue().size() > 0).map(e -> e.getKey()).collect(Collectors.toSet());
	}

	private void index(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		patents.add(patent.getDocNumber(), resume);
	}

	/**
	 * loads index from disk
	 * 
	 * @return success value
	 */
	public boolean load() {
		try {
			index = RandomAccessIndex.load();
			patents = (PatentIndex) ObjectReader.readObject(PATENTS_FILE);
		} catch (FileNotFoundException e) {
			return false;
		}
		return (index != null) && (patents != null);
	}

	/**
	 * matches given docNumbers to invention titles
	 * 
	 * @param docNumbers
	 *            a set of docNumbers
	 * @return list of invention titles
	 */
	public ArrayList<String> matchInventionTitles(Set<Integer> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Integer docNumber : docNumbers) {
			results.add(patents.get(docNumber).getInventionTitle());
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
		;
	}

	private Map<Integer, Set<Integer>> primitiveFind(String token) {
		return index.find(token);
	}

	public boolean write() {
		return index.saveToDisk() && ObjectWriter.writeObject(patents, PATENTS_FILE);
	}

}
