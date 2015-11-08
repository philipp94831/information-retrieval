package de.hpi.ir.yahoogle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.EliasDeltaReader;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class YahoogleIndex {

	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // 20MB
	protected static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = SearchEngineYahoogle.teamDirectory + "/offsets.yahoogle";
	private static final String PATENTS_FILE = SearchEngineYahoogle.teamDirectory + "/patents.yahoogle";
	private static final String POSTINGS_FILE = SearchEngineYahoogle.teamDirectory + "/postings.yahoogle";
	private static final String TMP_POSTINGS_FILE = SearchEngineYahoogle.teamDirectory + "/tmp.postings.yahoogle";

	private OffsetsIndex lastBlockOffsets;
	private RandomAccessFile tmp_index, index;
	private PatentIndex patents;
	private OffsetsIndex tmp_tokenOffsets, tokenOffsets;
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
		boolean status = YahoogleUtils.deleteIfExists(PATENTS_FILE) && YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE) && YahoogleUtils.deleteIfExists(POSTINGS_FILE) && YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		try {
			tmp_index = new RandomAccessFile(TMP_POSTINGS_FILE, "rw");
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenMap = new HashMap<String, YahoogleTokenMap>();
		lastBlockOffsets = new OffsetsIndex();
		tmp_tokenOffsets = new OffsetsIndex();
		tokenOffsets = new OffsetsIndex();
		patents = new PatentIndex();
		return status && (tmp_index != null) && (index != null);
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
			for (String t : tokenOffsets.getTokensForPrefix(pre)) {
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
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (OffsetsIndex) ObjectReader.readObject(OFFSETS_FILE);
		patents = (PatentIndex) ObjectReader.readObject(PATENTS_FILE);
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
		Map<Integer, Set<Integer>> docNumbers = new HashMap<Integer, Set<Integer>>();
		Long offset = tokenOffsets.get(token);
		if (offset != null) {
			try {
				index.seek(offset);
				int size = index.readInt();
				byte[] b = new byte[size];
				index.readFully(b);
				int i = 0;
				while (i < b.length) {
					AbstractReader in = new ByteReader(b, i, Integer.BYTES + Short.BYTES);
					i += Integer.BYTES + Short.BYTES;
					int docNumber = in.readInt();
					short bsize = in.readShort();
					in = new EliasDeltaReader(b, i, bsize);
					Set<Integer> pos = new HashSet<Integer>();
					int oldPos = 0;
					while (in.hasLeft()) {
						short p = in.readShort();
						pos.add(oldPos + p);
						oldPos += p;
					}
					docNumbers.put(docNumber, pos);
					i += bsize;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docNumbers;
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
					ByteWriter bout = new ByteWriter();
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

	public boolean write() {
		return ObjectWriter.writeObject(tokenOffsets, OFFSETS_FILE) && ObjectWriter.writeObject(patents, PATENTS_FILE);
	}

}
