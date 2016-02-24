package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import SearchEngine.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialCitationIndex;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.FileUtils;
import de.hpi.ir.yahoogle.util.MergeSortIterator;

public class CitationIndex extends Loadable {

	private static final String FILE_NAME = "citations";
	private static final Logger LOGGER = Logger
			.getLogger(CitationIndex.class.getName());

	private static String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + FILE_NAME
				+ FILE_EXTENSION;
	}

	private RandomAccessFile file;
	private IntegerOffsetsIndex offsets;

	@Override
	public void create() throws IOException {
		FileUtils.deleteIfExists(fileName());
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
		MergeSortIterator<PartialCitationIndex, BinaryCitationList, Integer> postingLists = new MergeSortIterator<>(
				indexes);
		while (postingLists.hasNext()) {
			List<BinaryCitationList> postingListList = postingLists.next();
			BinaryCitationList currentPostings = null;
			for (BinaryCitationList postingList : postingListList) {
				if (currentPostings == null) {
					currentPostings = postingList;
					continue;
				}
				currentPostings.append(postingList);
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

	private void writePostingList(BinaryCitationList postingList) throws IOException {
		long currentOffset = file.length();
		offsets.put(postingList.getDocNumber(), currentOffset);
		file.seek(currentOffset);
		byte[] bytes = postingList.getBytes();
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}
}
