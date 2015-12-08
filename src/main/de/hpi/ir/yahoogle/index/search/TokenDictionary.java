package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.BinaryPostingList;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.generation.PartialTokenDictionary;
import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.EliasDeltaReader;

public class TokenDictionary extends Loadable {

	private static final String FILE_NAME = "dictionary";
	private RandomAccessFile file;
	private OffsetsIndex<String> offsets;

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new OffsetsIndex<String>(new StringKeyReaderWriter(), FILE_NAME);
		offsets.create();
	}

	protected String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME + FILE_EXTENSION;
	}

	public Map<Integer, Set<Integer>> find(String token) {
		Map<Integer, Set<Integer>> docNumbers = new HashMap<Integer, Set<Integer>>();
		Long offset;
		try {
			offset = offsets.get(token);
			if (offset != null) {
				try {
					file.seek(offset);
					int size = file.readInt();
					byte[] b = new byte[size];
					file.readFully(b);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Set<String> getTokens() {
		try {
			return offsets.keys();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getTokensForPrefix(String prefix) {
		try {
			return offsets.keys().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new OffsetsIndex<>(new StringKeyReaderWriter(), FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialTokenDictionary> indexes) throws IOException {
		List<Iterator<BinaryPostingList>> iterators = indexes.stream().map(i -> i.iterator())
				.collect(Collectors.toList());
		TreeMap<BinaryPostingList, Integer> candidates = new TreeMap<BinaryPostingList, Integer>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<BinaryPostingList> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		BinaryPostingList currentPostings = null;
		while (!candidates.isEmpty()) {
			Entry<BinaryPostingList, Integer> entry = candidates.pollFirstEntry();
			Iterator<BinaryPostingList> iterator = iterators.get(entry.getValue());
			if (iterator.hasNext()) {
				candidates.put(iterator.next(), entry.getValue());
			}
			BinaryPostingList postingList = entry.getKey();
			String token = postingList.getToken();
			if (currentPostings == null) {
				currentPostings = postingList;
				continue;
			}
			if (!token.equals(currentPostings.getToken())) {
				writePostingList(currentPostings);
				currentPostings = postingList;
			} else {
				currentPostings.append(postingList.getBytes());
			}
		}
		writePostingList(currentPostings);
	}

	@Override
	public void write() throws IOException {
		file.close();
		offsets.write();
	}

	public void writePostingList(BinaryPostingList postingList) throws IOException {
		long offset = file.length();
		offsets.put(postingList.getToken(), offset);
		byte[] bytes = postingList.getBytes();
		file.seek(offset);
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}

}
