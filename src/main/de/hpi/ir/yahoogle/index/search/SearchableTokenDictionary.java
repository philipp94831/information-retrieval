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
import de.hpi.ir.yahoogle.index.DocumentPosting;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PostingList;
import de.hpi.ir.yahoogle.index.generation.TokenDictionary;
import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.EliasDeltaReader;

public class SearchableTokenDictionary extends Loadable {

	private static final String FILE_NAME = "dictionary";
	private RandomAccessFile file;
	private OffsetsIndex<String> offsets;

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new OffsetsIndex<>(new StringKeyReaderWriter(), FILE_NAME);
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

	public void merge(List<TokenDictionary> indexes) throws IOException {
		List<Iterator<PostingList>> iterators = indexes.stream().map(i -> i.iterator()).collect(Collectors.toList());
		TreeMap<PostingList, Integer> candidates = new TreeMap<PostingList, Integer>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<PostingList> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		PostingList currentPostings = null;
		while (!candidates.isEmpty()) {
			Entry<PostingList, Integer> entry = candidates.pollFirstEntry();
			PostingList postingList = entry.getKey();
			String token = postingList.getToken();
			if (currentPostings == null) {
				currentPostings = new PostingList(token);
			}
			if (!token.equals(currentPostings.getToken())) {
				writePostingList(currentPostings);
				currentPostings = new PostingList(token);
			}
			for (DocumentPosting postings : postingList) {
				currentPostings.add(postings);
			}
			Iterator<PostingList> iterator = iterators.get(entry.getValue());
			if (iterator.hasNext()) {
				candidates.put(iterator.next(), entry.getValue());
			}
		}
	}

	@Override
	public void write() throws IOException {
		file.close();
		offsets.write();
	}

	public void writePostingList(PostingList postingList) throws IOException {
		long offset = file.length();
		offsets.put(postingList.getToken(), offset);
		byte[] bytes = postingList.toByteArray();
		file.seek(offset);
		file.writeInt(bytes.length);
		file.write(bytes);
	}

}
