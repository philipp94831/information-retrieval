package de.hpi.ir.yahoogle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.EliasDeltaReader;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class RandomAccessIndex {
	
	private static final String OFFSETS_FILE = SearchEngineYahoogle.teamDirectory + "/offsets.yahoogle";
	private static final String POSTINGS_FILE = SearchEngineYahoogle.teamDirectory + "/postings.yahoogle";
	
	protected RandomAccessFile index;
	protected OffsetsIndex offsets;
	
	public static RandomAccessIndex load() throws FileNotFoundException {	
		return new RandomAccessIndex((OffsetsIndex) ObjectReader.readObject(OFFSETS_FILE));
	}
	
	public static RandomAccessIndex create() {
		YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		YahoogleUtils.deleteIfExists(POSTINGS_FILE);
		return new RandomAccessIndex(new OffsetsIndex());
	}

	public RandomAccessIndex() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public RandomAccessIndex(OffsetsIndex offsets) {
		this();
		this.offsets = offsets;
	}

	public List<String> getTokensForPrefix(String prefix) {
		return offsets.getTokensForPrefix(prefix);
	}

	public Map<Integer, Set<Integer>> find(String token) {
		Map<Integer, Set<Integer>> docNumbers = new HashMap<Integer, Set<Integer>>();
		Long offset = offsets.get(token);
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

	public boolean saveToDisk() {
		return ObjectWriter.writeObject(offsets, OFFSETS_FILE);
	}

	public void add(String token, byte[] byteArray) throws IOException {
		long start = index.length();
		offsets.put(token, start);
		index.seek(start);
		index.writeInt(byteArray.length);
		index.write(byteArray);
	}

}
