package de.hpi.ir.yahoogle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class YahoogleInMemoryIndex {

	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String TMP_POSTINGS_FILE = "tmp.postings.yahoogle";
	private static final int POST_SIZE = Short.BYTES;
	
	private Map<String, YahoogleTokenMap> tokenMap = new HashMap<String, YahoogleTokenMap>();
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();
	private RandomAccessFile tmp_index, index;

	public void add(String token, int docNumber, YahoogleIndexPosting posting) {
		if (tokenMap.get(token) == null) {
			tokenMap.put(token, new YahoogleTokenMap());
		}
		tokenMap.get(token).add(docNumber, posting);
	}

	public void flush() {
		for (Entry<String, YahoogleTokenMap> entry : tokenMap.entrySet()) {
			entry.getValue().write(entry.getKey(), tmp_index);
		}
	}

	public void reorganize() {
		try {
			for (Entry<String, YahoogleTokenMap> entry : tokenMap.entrySet()) {
				long start = index.length();
				tokenOffsets.put(entry.getKey(), start);
				entry.getValue().reorganize(index, tmp_index, start);
			}
			tmp_index.close();
			YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
	public Set<Integer> find(String token) {
		Set<Integer> docNumbers = new HashSet<Integer>();
		Long offset = tokenOffsets.get(token);
		if (offset != null) {
			try {
				index.seek(offset);
				int size = index.readInt();
				byte[] b = new byte[size];
				index.readFully(b);
				int i = 0;
				while(i < b.length) {
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

	public boolean create() {
		boolean status = YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE)
				&& YahoogleUtils.deleteIfExists(POSTINGS_FILE) && YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		try {
			tmp_index = new RandomAccessFile(TMP_POSTINGS_FILE, "rw");
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		return status;
	}

	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) YahoogleUtils.loadObject(OFFSETS_FILE);
		return true;
	}

	public boolean write() {
		return YahoogleUtils.writeObject(tokenOffsets, OFFSETS_FILE);
	}	

}
