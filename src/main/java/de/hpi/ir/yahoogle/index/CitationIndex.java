package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.CitationListIterator;
import de.hpi.ir.yahoogle.index.partial.PartialCitationIndex;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class CitationIndex extends Loadable {

	private static final String FILE_NAME = "citations";
	private static final Logger LOGGER = Logger
			.getLogger(CitationIndex.class.getName());

	private static String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME
				+ FILE_EXTENSION;
	}

	private RandomAccessFile file;
	private IntegerOffsetsIndex offsets;

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new IntegerOffsetsIndex(FILE_NAME);
		offsets.create();
	}

	public List<Integer> find(Integer docNumber) {
		try {
			Long offset = offsets.get(docNumber);
			if (offset != null) {
				file.seek(offset);
				int size = file.readInt();
				byte[] b = new byte[size];
				file.read(b);
				BinaryCitationList postingList = new BinaryCitationList(
						docNumber, b);
				return postingList.getDocNumbers();
			}
		} catch (IOException e) {
			LOGGER.severe(
					"Error reading citation list for docNumber " + docNumber);
		}
		return new ArrayList<>();
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		offsets = new IntegerOffsetsIndex(FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialCitationIndex> indexes) throws IOException {
		LOGGER.info("Merging citation indices");
		List<CitationListIterator> iterators = indexes.stream()
				.map(PartialCitationIndex::iterator)
				.collect(Collectors.toList());
		TreeMap<BinaryCitationList, Integer> candidates = new TreeMap<>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<BinaryCitationList> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		BinaryCitationList currentPostings = null;
		while (!candidates.isEmpty()) {
			Entry<BinaryCitationList, Integer> entry = candidates
					.pollFirstEntry();
			Iterator<BinaryCitationList> iterator = iterators
					.get(entry.getValue());
			if (iterator.hasNext()) {
				candidates.put(iterator.next(), entry.getValue());
			}
			BinaryCitationList postingList = entry.getKey();
			int token = postingList.getDocNumber();
			if (currentPostings == null) {
				startPostingList(token);
				currentPostings = postingList;
				continue;
			}
			if (token != currentPostings.getDocNumber()) {
				writePostingList(currentPostings);
				startPostingList(token);
				currentPostings = postingList;
			} else {
				byte[] newBytes = postingList.getBytes();
				currentPostings.append(newBytes);
			}
		}
		writePostingList(currentPostings);
	}

	private void startPostingList(int token) throws IOException {
		long currentOffset = file.length();
		offsets.put(token, currentOffset);
		file.seek(currentOffset);
	}

	public void warmUp() {
	}

	@Override
	public void write() throws IOException {
		file.close();
		offsets.write();
	}

	private void writePostingList(BinaryCitationList postingList) throws IOException {
		byte[] bytes = postingList.getBytes();
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}
}
