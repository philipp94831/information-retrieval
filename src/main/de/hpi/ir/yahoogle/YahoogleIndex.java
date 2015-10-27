package de.hpi.ir.yahoogle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // write
																		// to
																		// file
																		// at
																		// 20MB
																		// free
																		// memory
	private static final String PATENTS_FILE = "patents.yahoogle";
	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private YahoogleInMemoryIndex indexBuffer = new YahoogleInMemoryIndex();

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	public void add(Patent patent) {
		setInventionTitle(patent);
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for (short i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = sanitize(tokenizer.nextToken());
			if (stopwords.contains(token)) {
				continue;
			}
			YahoogleIndexPosting posting = new YahoogleIndexPosting();
			posting.setPosition(i);
			buffer(token, patent.getDocNumber(), posting);
		}

		if (Runtime.getRuntime().freeMemory() < FLUSH_MEM_THRESHOLD) {
			flush();
		}
	}

	public boolean create() {
		boolean status = YahoogleUtils.deleteIfExists(PATENTS_FILE);
		indexBuffer.create();
		return status;
	}

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String token) {
		token = sanitize(token);
		return indexBuffer.find(token);
	}

	/**
	 * flushes indexBuffer and reorganzie temporary index to final index
	 */
	public void finish() {
		flush();
		reorganize();
	}

	/**
	 * writes indexBuffer to temporary index on disk
	 */
	private void flush() {
		indexBuffer.flush();
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
		indexBuffer.load();
		patents = (Map<Integer, PatentResume>) YahoogleUtils.loadObject(PATENTS_FILE);
		return true;
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
			results.add(getInventionTitle(docNumber));
		}
		return results;
	}

	private void buffer(String token, int docNumber, YahoogleIndexPosting posting) {
		indexBuffer.add(token, docNumber, posting);
	}

	private void reorganize() {
		indexBuffer.reorganize();
	}

	private String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	private void setInventionTitle(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		patents.put(patent.getDocNumber(), resume);
	}

	public boolean write() {
		return indexBuffer.write()
				&& YahoogleUtils.writeObject(patents, PATENTS_FILE);
	}

}
