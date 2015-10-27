package de.hpi.ir.yahoogle;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB
	protected static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String PATENTS_FILE = "patents.yahoogle";
	protected static final int POST_SIZE = Short.BYTES;
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String TMP_POSTINGS_FILE = "tmp.postings.yahoogle";

	private Map<String, Long> lastBlockOffsets = new HashMap<String, Long>();
	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private RandomAccessFile tmp_index, index;
	private Map<String, Long> tmp_tokenOffsets = new HashMap<String, Long>();
	private Map<String, YahoogleTokenMap> tokenMap = new HashMap<String, YahoogleTokenMap>();

	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();

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
		boolean status = YahoogleUtils.deleteIfExists(PATENTS_FILE) && YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE) && YahoogleUtils.deleteIfExists(POSTINGS_FILE) && YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		try {
			tmp_index = new RandomAccessFile(TMP_POSTINGS_FILE, "rw");
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		return status && (tmp_index != null) && (index != null);
	}

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String token) {
		token = sanitize(token);
		Set<Integer> docNumbers = new HashSet<Integer>();
		Long offset = tokenOffsets.get(token);
		if (offset != null) {
			try {
				index.seek(offset);
				int size = index.readInt();
				byte[] b = new byte[size];
				index.readFully(b);
				int i = 0;
				while (i < b.length) {
					ByteBuffer bb = ByteBuffer.wrap(b, i, Integer.BYTES + Short.BYTES);
					docNumbers.add(bb.getInt());
					short postingNumber = bb.getShort();
					i += Integer.BYTES + Short.BYTES + POST_SIZE * postingNumber;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docNumbers;
	}

	/**
	 * flushes indexBuffer and reorganzie temporary index to final index
	 */
	public void finish() {
		flush();
		reorganize();
	}

	public void flush() {
		try {
			for (Entry<String, YahoogleTokenMap> entry : tokenMap.entrySet()) {
				long fileLength = tmp_index.length();
				if (tmp_tokenOffsets.get(entry.getKey()) == null) {
					tmp_tokenOffsets.put(entry.getKey(), fileLength);
				}
				Long offset = lastBlockOffsets.get(entry.getKey());
				lastBlockOffsets.put(entry.getKey(), fileLength);
				entry.getValue().write(entry.getKey(), offset, tmp_index);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokenMap.clear();
	}

	private String getInventionTitle(Integer docNumber) {
		return patents.get(docNumber).getInventionTitle();
	}

	private void index(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		patents.put(patent.getDocNumber(), resume);
	}

	/**
	 * loads index from disk
	 * 
	 * @return success value
	 */
	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) YahoogleUtils.loadObject(OFFSETS_FILE);
		patents = (Map<Integer, PatentResume>) YahoogleUtils.loadObject(PATENTS_FILE);
		return (index != null) && (tokenOffsets != null) && (patents != null);
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

	public void reorganize() {
		try {
			for (Entry<String, Long> entry : tmp_tokenOffsets.entrySet()) {
				long start = index.length();
				tokenOffsets.put(entry.getKey(), start);
				try {
					int total_size = 0;
					long offset = entry.getValue();
					long next = offset;
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					while (next != NO_NEXT_POSTING) {
						tmp_index.seek(offset);
						next = tmp_index.readLong();
						int size = tmp_index.readInt();
						total_size += size;
						byte[] b = new byte[size];
						tmp_index.readFully(b);
						bout.write(b);
					}
					index.seek(start);
					index.writeInt(total_size);
					index.write(bout.toByteArray());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			tmp_index.close();
			YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	public boolean write() {
		return YahoogleUtils.writeObject(tokenOffsets, OFFSETS_FILE) && YahoogleUtils.writeObject(patents, PATENTS_FILE);
	}

}
