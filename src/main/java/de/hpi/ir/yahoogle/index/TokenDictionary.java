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

	private long currentOffset;
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
				int count = file.readInt();
				Map<Integer, Set<Integer>> result = new HashMap<>();
				for (int i = 0; i < count; i++) {
					int size = file.readInt();
					byte[] b = new byte[size];
					file.readFully(b);
					BinaryPostingList postingList = new BinaryPostingList(token,
							b);
					result.putAll(postingList.getDocumentsWithPositions());
				}
				return result;
			}
		} catch (IOException e) {
			LOGGER.severe("Error reading posting list for token " + token);
		}
		return new HashMap<>();
	}

	private void finishPostingList(int count) throws IOException {
		file.seek(currentOffset);
		file.writeInt(count);
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
		LOGGER.info("Merging token dictionaries");
		List<Iterator<BinaryPostingList>> iterators = indexes.stream()
				.map(PartialTokenDictionary::iterator)
				.collect(Collectors.toList());
		TreeMap<BinaryPostingList, Integer> candidates = new TreeMap<>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<BinaryPostingList> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		BinaryPostingList currentPostings = null;
		int count = 0;
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
				startPostingList(token);
				count = 1;
				currentPostings = postingList;
				continue;
			}
			if (!token.equals(currentPostings.getToken())) {
				writePostingList(currentPostings);
				finishPostingList(count);
				startPostingList(token);
				count = 1;
				currentPostings = postingList;
			} else {
				byte[] newBytes = postingList.getBytes();
				if (currentPostings.hasSpaceLeft(newBytes)) {
					currentPostings.append(newBytes);
				} else {
					writePostingList(currentPostings);
					count++;
					currentPostings = postingList;
				}
			}
		}
		writePostingList(currentPostings);
		finishPostingList(count);
	}

	private void startPostingList(String token) throws IOException {
		currentOffset = file.length();
		offsets.put(token, currentOffset);
		file.seek(currentOffset);
		file.writeInt(0);
	}

	@Override
	public void write() throws IOException {
		file.close();
		offsets.write();
	}

	private void writePostingList(BinaryPostingList postingList) throws IOException {
		byte[] bytes = postingList.getBytes();
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}

	public void warmUp() {
		// TODO Auto-generated method stub
		
	}
}
