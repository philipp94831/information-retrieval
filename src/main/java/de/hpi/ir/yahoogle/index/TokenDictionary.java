package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialTokenDictionary;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.MergeSortIterator;

public class TokenDictionary extends Loadable {

	private static final String FILE_NAME = "dictionary";
	private static final Logger LOGGER = Logger.getLogger(TokenDictionary.class.getName());

	private static String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME + FILE_EXTENSION;
	}

	private long currentOffset;
	private RandomAccessFile file;
	private StringOffsetsIndex offsets;

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new StringOffsetsIndex(FILE_NAME);
		offsets.create();
	}

	public BinaryPostingListIterator find(String token) {
		try {
			Long offset = offsets.get(token);
			if (offset != null) {
				file.seek(offset);
				int count = file.readInt();
				return new BinaryPostingListIterator(this, count, file.getFilePointer());
			}
		} catch (IOException e) {
			LOGGER.severe("Error initializing search for token " + token);
		}
		return null;
	}

	public BinaryPostingList getBinaryPostingList(long offset) throws IOException {
		file.seek(offset);
		int size = file.readInt();
		byte[] b = new byte[size];
		file.readFully(b);
		return new BinaryPostingList(b);
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
		offsets = new StringOffsetsIndex(FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialTokenDictionary> indexes) throws IOException {
		LOGGER.info("Merging token dictionaries");
		MergeSortIterator<PartialTokenDictionary, BinaryPostingList, String> postingLists = new MergeSortIterator<>(indexes);
		while (postingLists.hasNext()) {
			List<BinaryPostingList> postingListList = postingLists.next();
			PartitionedBinaryPostingList currentPostings = null;
			for(BinaryPostingList postingList : postingListList) {
				if (currentPostings == null) {
					currentPostings = new PartitionedBinaryPostingList(postingList);
					continue;
				}
				currentPostings.add(postingList);
			}
			if (currentPostings != null) {
				writePostingList(currentPostings);
			}
		}
	}

	public void warmUp() {
	}

	@Override
	public void write() throws IOException {
		file.close();
		offsets.write();
	}

	private void writePostingList(PartitionedBinaryPostingList postingList) throws IOException {
		currentOffset = file.length();
		offsets.put(postingList.getToken(), currentOffset);
		file.seek(currentOffset);
		List<byte[]> blocks = postingList.getSortedBlocks();
		file.writeInt(blocks.size());
		if(blocks.size() > 1) {
			LOGGER.info("writing " + blocks.size() + " blocks for token " + postingList.getToken());
		}
		for (byte[] bytes : blocks) {
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.write(bytes);
			file.write(out.toByteArray());
		}
	}
}
