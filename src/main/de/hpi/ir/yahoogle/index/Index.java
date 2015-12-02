package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.PatentParserCallback;
import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.StopWordList;
import de.hpi.ir.yahoogle.rm.Model;
import de.hpi.ir.yahoogle.rm.ModelResult;
import de.hpi.ir.yahoogle.rm.QLModel;

public class Index implements PatentParserCallback {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB

	private LinkedRandomAccessIndex linkedIndex;
	private OrganizedRandomAccessIndex organizedIndex;
	private PatentIndex patents;
	private IndexBuffer indexBuffer;
	private String patentsFolder;
	
	public Index(String patentsFolder) {
		this.patentsFolder = patentsFolder;
	}

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	@Override
	public void callback(Patent patent) {
		PatentResume resume = new PatentResume(patent);		
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		int i = 1;
		while(tokenizer.hasMoreTokens()) {
			String token = Stemmer.stem(tokenizer.nextToken());
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
		linkedIndex = LinkedRandomAccessIndex.create();
		indexBuffer = new IndexBuffer(linkedIndex);
		patents = new PatentIndex(patentsFolder);
		try {
			patents.create();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (linkedIndex != null) && (patents != null);
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
			return organizedIndex.find(Stemmer.stem(token));
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
			patents = new PatentIndex(patentsFolder);
			patents.load();
		} catch (IOException e) {
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

	private static void merge(Map<Integer, Set<Integer>> result, Map<Integer, Set<Integer>> newResult) {
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
		try {
			patents.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return organizedIndex.saveToDisk();
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

	public PatentResume getPatent(int docNumber) {
		return patents.get(docNumber);
	}

}
