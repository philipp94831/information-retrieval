package de.hpi.ir.yahoogle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB
	private static final String PATENTS_FILE = "patents.yahoogle";

	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private YahoogleOnDiskIndex diskIndex = new YahoogleOnDiskIndex();

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	public void add(Patent patent) {
		index(patent);
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for (short i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = sanitize(tokenizer.nextToken());
			if (YahoogleUtils.isStopword(token)) {
				continue;
			}
			YahoogleIndexPosting posting = new YahoogleIndexPosting();
			posting.setPosition(i);
			diskIndex.add(token, patent.getDocNumber(), posting);
		}

		if (Runtime.getRuntime().freeMemory() < FLUSH_MEM_THRESHOLD) {
			diskIndex.flush();
		}
	}

	public boolean create() {
		return YahoogleUtils.deleteIfExists(PATENTS_FILE) && diskIndex.create();
	}

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String token) {
		return diskIndex.find(sanitize(token));
	}

	/**
	 * flushes indexBuffer and reorganzie temporary index to final index
	 */
	public void finish() {
		diskIndex.flush();
		diskIndex.reorganize();
	}

	private String getInventionTitle(Integer docNumber) {
		return patents.get(docNumber).getInventionTitle();
	}

	/**
	 * loads index from disk
	 * 
	 * @return success value
	 */
	@SuppressWarnings("unchecked")
	public boolean load() {
		patents = (Map<Integer, PatentResume>) YahoogleUtils.loadObject(PATENTS_FILE);
		return diskIndex.load() && (patents != null);
	}

	/**
	 * matches given docNumbers to invention titles
	 * 
	 * @param docNumbers a set of docNumbers
	 * @return list of invention titles
	 */
	public ArrayList<String> matchInventionTitles(Set<Integer> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Integer docNumber : docNumbers) {
			results.add(getInventionTitle(docNumber));
		}
		return results;
	}

	private String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	private void index(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		patents.put(patent.getDocNumber(), resume);
	}

	public boolean write() {
		return diskIndex.write() && YahoogleUtils.writeObject(patents, PATENTS_FILE);
	}

}
