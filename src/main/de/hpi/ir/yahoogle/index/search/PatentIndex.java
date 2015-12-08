package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.index.generation.PartialPatentIndex;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentIndex extends Loadable {

	private static final String FILE_NAME = "patents";

	protected static final long TOTAL_WORD_COUNT_OFFSET = 0;
	protected RandomAccessFile file;

	private OffsetsIndex<Integer> offsets;

	private String patentsFolder;

	protected int totalWordCount = 0;

	public PatentIndex(String patentsFolder) {
		this.patentsFolder = patentsFolder;
	}

	public void add(PatentResume resume) {
		totalWordCount += resume.getWordCount();
		try {
			byte[] bytes = resume.toByteArray();
			long offset = file.length();
			file.seek(offset);
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
		file.writeInt(0); // totalWordCount
		offsets = new OffsetsIndex<Integer>(new IntegerKeyReaderWriter(), FILE_NAME);
		offsets.create();
	}

	protected String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + FILE_NAME + FILE_EXTENSION;
	}

	public PatentResume get(Integer docNumber) {
		try {
			long offset = offsets.get(docNumber);
			byte[] bytes = read(offset);
			PatentResume resume = PatentResume.fromByteArray(docNumber, bytes);
			resume.setPatentFolder(patentsFolder);
			return resume;
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
		offsets = new OffsetsIndex<Integer>(new IntegerKeyReaderWriter(), FILE_NAME);
		offsets.load();
	}

	public void merge(List<PartialPatentIndex> indexes) {
		List<Iterator<PatentResume>> iterators = indexes.stream().map(i -> i.iterator()).collect(Collectors.toList());
		TreeMap<PatentResume, Integer> candidates = new TreeMap<PatentResume, Integer>();
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

	public byte[] read(long offset) throws IOException {
		file.seek(offset);
		int length = file.readInt();
		byte[] bytes = new byte[length];
		file.read(bytes);
		return bytes;
	}

	public int wordCount(Integer docNumber) {
		return get(docNumber).getWordCount();
	}

	@Override
	public void write() throws IOException {
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(totalWordCount);
		file.close();
		offsets.write();
	}

}
