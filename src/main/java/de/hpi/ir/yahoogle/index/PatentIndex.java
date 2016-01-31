package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialPatentIndex;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentIndex extends Loadable {

	private static final String FILE_NAME = "patents";
	private static final Logger LOGGER = Logger
			.getLogger(PatentIndex.class.getName());
	private static final int MAX_CACHE_SIZE = 100000;
	private static final long TOTAL_WORD_COUNT_OFFSET = 0L;
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
		PatentResume resume = cache.get(docNumber);
		if (resume != null) {
			return resume;
		}
		try {
			long offset = offsets.get(docNumber);
			resume = new PatentResume(read(offset));
			resume.setPatentFolder(patentsFolder);
			cache.put(resume.getDocNumber(), resume);
			return resume;
		} catch (IOException e) {
			LOGGER.severe("Error loading patent " + docNumber + " from disk");
		}
		return null;
	}

	public Set<Integer> getAllDocNumbers() {
		try {
			return offsets.keys();
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
		List<Iterator<PatentResume>> iterators = indexes.stream()
				.map(PartialPatentIndex::iterator).collect(Collectors.toList());
		TreeMap<PatentResume, Integer> candidates = new TreeMap<>();
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<PatentResume> iterator = iterators.get(i);
			candidates.put(iterator.next(), i);
		}
		while (!candidates.isEmpty()) {
			Entry<PatentResume, Integer> entry = candidates.pollFirstEntry();
			add(entry.getKey());
			Iterator<PatentResume> iterator = iterators.get(entry.getValue());
			if (iterator.hasNext()) {
				candidates.put(iterator.next(), entry.getValue());
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

	public void warmUp() {
		try {
			List<Integer> docNumbers = new ArrayList<>(offsets.keys());
			Random rand = new Random();
			for (int i = 0; i < MAX_CACHE_SIZE / 2 && !docNumbers.isEmpty(); i++) {
				int index = rand.nextInt(docNumbers.size());
				get(docNumbers.get(index));
				docNumbers.remove(index);
			}
		} catch (IOException e) {
			LOGGER.severe("Error reading patents when warming up");
		}
	}

	@Override
	public void write() throws IOException {
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(totalWordCount);
		file.close();
		offsets.write();
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
}
