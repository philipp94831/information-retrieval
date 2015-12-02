package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.YahoogleUtils;
import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.EliasDeltaReader;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class OrganizedRandomAccessIndex extends AbstractRandomAccessIndex {

	private static final String OFFSETS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/offsets.yahoogle";
	private static final String POSTINGS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/postings.yahoogle";

	public static OrganizedRandomAccessIndex create() {
		YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		YahoogleUtils.deleteIfExists(POSTINGS_FILE);
		return new OrganizedRandomAccessIndex(new OffsetsIndex<String>());
	}

	public static OrganizedRandomAccessIndex load() throws FileNotFoundException {
		return new OrganizedRandomAccessIndex(ObjectReader.<OffsetsIndex<String>>readObject(OFFSETS_FILE));
	}

	public OrganizedRandomAccessIndex() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public OrganizedRandomAccessIndex(OffsetsIndex<String> offsets) {
		this();
		this.offsets = offsets;
	}

	public void add(String token, byte[] byteArray) throws IOException {
		long start = index.length();
		offsets.put(token, start);
		index.seek(start);
		index.writeInt(byteArray.length);
		index.write(byteArray);
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

	public List<String> getTokensForPrefix(String prefix) {
		return offsets.keys().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
	}
	
	public Set<String> getTokens() {
		return offsets.keys();
	}

	public boolean saveToDisk() {
		return ObjectWriter.writeObject(offsets, OFFSETS_FILE);
	}

}
