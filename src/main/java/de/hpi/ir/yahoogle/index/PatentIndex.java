package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialPatentIndex;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentIndex extends Loadable {

	private static final String FILE_NAME = "patents";
	private static final int MAX_BYTE_READ = 1024 * 128;
	private static final int MAX_CACHE_SIZE = 100000;
	private static final long TOTAL_WORD_COUNT_OFFSET = 0L;
	private final Map<Integer, PatentResume> cache = new HashMap<>();
	private RandomAccessFile file;
	private IntegerOffsetsIndex offsets;
	private final String patentsFolder;
	private int totalWordCount = 0;

	public PatentIndex(String patentsFolder) {
		this.patentsFolder = patentsFolder;
	}

	private void add(PatentResume resume) {
		totalWordCount += resume.getWordCount();
		try {
			byte[] bytes = resume.toByteArray();
			long offset = file.getFilePointer();
			offsets.put(resume.getDocNumber(), offset);
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.write(bytes);
			file.write(out.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private void freeCache() {
		Set<Integer> toRemove = cache.keySet().stream()
				.limit(MAX_CACHE_SIZE / 2).collect(Collectors.toSet());
		cache.keySet().removeAll(toRemove);
	}

	public PatentResume get(Integer docNumber) {
		PatentResume resume = cache.get(docNumber);
		if (resume != null) {
			return resume;
		}
		if (cache.size() > MAX_CACHE_SIZE) {
			freeCache();
		}
		try {
			long offset = offsets.get(docNumber);
			List<byte[]> bytes = read(offset);
			for (byte[] b : bytes) {
				resume = new PatentResume(b);
				resume.setPatentFolder(patentsFolder);
				cache.put(resume.getDocNumber(), resume);
			}
			return cache.get(docNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Set<Integer> getAllDocNumbers() {
		try {
			return offsets.keys();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void merge(List<PartialPatentIndex> indexes) {
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

	private List<byte[]> read(long offset) throws IOException {
		file.seek(offset);
		int size = Math.min((int) (file.length() - file.getFilePointer()),
				MAX_BYTE_READ);
		byte[] large = new byte[size];
		file.read(large);
		ByteReader in = new ByteReader(large);
		List<byte[]> list = new ArrayList<>();
		while (true) {
			if (in.remaining() < Integer.BYTES) {
				break;
			}
			int length = in.readInt();
			if (in.remaining() < length) {
				break;
			}
			byte[] bytes = in.read(length);
			list.add(bytes);
		}
		return list;
	}

	@Override
	public void write() throws IOException {
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(totalWordCount);
		file.close();
		offsets.write();
	}
}
