package de.hpi.ir.yahoogle;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class YahoogleOnDiskIndex {

	protected static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	protected static final int POST_SIZE = Short.BYTES;
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String TMP_POSTINGS_FILE = "tmp.postings.yahoogle";

	private Map<String, Long> lastBlockOffsets = new HashMap<String, Long>();
	private RandomAccessFile tmp_index, index;
	private Map<String, Long> tmp_tokenOffsets = new HashMap<String, Long>();
	private Map<String, YahoogleTokenMap> tokenMap = new HashMap<String, YahoogleTokenMap>();
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();

	public void add(String token, int docNumber, YahoogleIndexPosting posting) {
		if (tokenMap.get(token) == null) {
			tokenMap.put(token, new YahoogleTokenMap());
		}
		tokenMap.get(token).add(docNumber, posting);
	}

	public boolean create() {
		boolean status = YahoogleUtils.deleteIfExists(TMP_POSTINGS_FILE)
				&& YahoogleUtils.deleteIfExists(POSTINGS_FILE)
				&& YahoogleUtils.deleteIfExists(OFFSETS_FILE);
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

	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) YahoogleUtils.loadObject(OFFSETS_FILE);
		return (index != null) && (tokenOffsets != null);
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

	public boolean write() {
		return YahoogleUtils.writeObject(tokenOffsets, OFFSETS_FILE);
	}

}
