package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialTokenDictionary;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class TokenDictionary extends Loadable {

	private static final String FILE_NAME = "dictionary";
	private static final Logger LOGGER = Logger
			.getLogger(TokenDictionary.class.getName());

	private static String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME
				+ FILE_EXTENSION;
	}

	private RandomAccessFile file;
	private StringOffsetIndex offsets;

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new StringOffsetIndex(FILE_NAME);
		offsets.create();
	}

	public Map<Integer, Set<Integer>> find(String token) {
		try {
			Long offset = offsets.get(token);
			if (offset != null) {
				file.seek(offset);
				int size = file.readInt();
				byte[] b = new byte[size];
				file.readFully(b);
				BinaryPostingList postingList = new BinaryPostingList(token, b);
				return postingList.getDocumentsWithPositions();
			}
		} catch (IOException e) {
			LOGGER.severe("Error reading posting list for token " + token);
		}
		return new HashMap<>();
	}

	public Set<String> getTokens() {
		try {
			return offsets.keys();
		} catch (IOException e) {
			LOGGER.severe("Error retrieving all tokens");
		}
		return null;
	}

	public List<String> getTokensForPrefix(String prefix) {
		try {
			return offsets.getKeysForPrefix(prefix);
		} catch (IOException e) {
			LOGGER.severe("Error retrieving tokens for prefix " + prefix);
		}
		return null;
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new StringOffsetIndex(FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialTokenDictionary> indexes) throws IOException {
		List<Iterator<BinaryPostingList>> iterators = indexes.stream()
				.map(PartialTokenDictionary::iterator)
				.collect(Collectors.toList());
		TreeMap<BinaryPostingList, Integer> candidates = new TreeMap<>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<BinaryPostingList> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		BinaryPostingList currentPostings = null;
		while (!candidates.isEmpty()) {
			Entry<BinaryPostingList, Integer> entry = candidates
					.pollFirstEntry();
			Iterator<BinaryPostingList> iterator = iterators
					.get(entry.getValue());
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

	private void writePostingList(BinaryPostingList postingList) throws IOException {
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
