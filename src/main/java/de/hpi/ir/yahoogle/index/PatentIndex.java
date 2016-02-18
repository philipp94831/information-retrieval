package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import SearchEngine.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialPatentIndex;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.MergeSortIterator;

public class PatentIndex extends Loadable {

	private static final String FILE_NAME = "patents";
	private static final Logger LOGGER = Logger
			.getLogger(PatentIndex.class.getName());
	private static final int MAX_CACHE_SIZE = 100000;
	private static final long TOTAL_WORD_COUNT_OFFSET = 0L;
	private static final boolean USE_CACHE = true;
	private final Map<Integer, PatentResume> cache = new LinkedHashMap<>(
			MAX_CACHE_SIZE, 0.75F, true);
	private RandomAccessFile file;
	private IntegerOffsetsIndex offsets;
	private final String patentsFolder;
	private int totalWordCount = 0;

	public PatentIndex(String patentsFolder) {
		this.patentsFolder = patentsFolder;
	}

	private void add(PatentResume resume) throws IOException {
		totalWordCount += resume.getWordCount();
		byte[] bytes = resume.toByteArray();
		long offset = file.getFilePointer();
		offsets.put(resume.getDocNumber(), offset);
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		file = new RandomAccessFile(fileName(), "rw");
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(-1); // totalWordCount
		offsets = new IntegerOffsetsIndex(FILE_NAME);
		offsets.create();
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME
				+ FILE_EXTENSION;
	}

	public PatentResume get(Integer docNumber) {
		if (USE_CACHE) {
			PatentResume resume = cache.get(docNumber);
			if (resume != null) {
				return resume;
			}
		}
		try {
			long offset = offsets.get(docNumber);
			PatentResume resume = new PatentResume(read(offset));
			resume.setPatentFolder(patentsFolder);
			if (USE_CACHE) {
				cache.put(resume.getDocNumber(), resume);
			}
			return resume;
		} catch (IOException e) {
			LOGGER.severe("Error loading patent " + docNumber + " from disk");
		}
		return null;
	}

	public Set<Integer> getAllDocNumbers() {
		try {
			return new HashSet<>(offsets.keys());
		} catch (IOException e) {
			LOGGER.severe("Error retrieving all docnumbers");
		}
		return null;
	}

	public int getTotalWordCount() {
		return totalWordCount;
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		this.totalWordCount = file.readInt();
		offsets = new IntegerOffsetsIndex(FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialPatentIndex> indexes) throws IOException {
		LOGGER.info("Merging patent indices");
		MergeSortIterator<PartialPatentIndex, PatentResume, Integer> patents = new MergeSortIterator<>(
				indexes);
		while (patents.hasNext()) {
			List<PatentResume> list = patents.next();
			for (PatentResume resume : list) {
				add(resume);
			}
		}
	}

	private byte[] read(long offset) throws IOException {
		file.seek(offset);
		int length = file.readInt();
		byte[] bytes = new byte[length];
		file.read(bytes);
		return bytes;
	}

	public void update(PatentResume resume) throws IOException {
		byte[] bytes = resume.toByteArray();
		long offset = offsets.get(resume.getDocNumber());
		file.seek(offset);
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
	}

	public void warmUp() throws IOException {
		if (USE_CACHE) {
			List<Integer> docNumbers = offsets.keys();
			Random rand = new Random();
			for (int i = 0; i < MAX_CACHE_SIZE / 2
					&& !docNumbers.isEmpty(); i++) {
				int index = rand.nextInt(docNumbers.size());
				get(docNumbers.get(index));
				docNumbers.remove(index);
			}
		}
	}

	@Override
	public void write() throws IOException {
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(totalWordCount);
		file.close();
		offsets.write();
	}
}
