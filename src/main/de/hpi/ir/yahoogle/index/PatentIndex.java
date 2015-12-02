package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.YahoogleUtils;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class PatentIndex implements Loadable {
	
	private static final String PATENTS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/patents.yahoogle";
	private static final String OFFSETS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/offsets.patents.yahoogle";
	private static final long TOTAL_WORD_COUNT_OFFSET = 0;
	
	private OffsetsIndex<Integer> offsets;
	private int totalWordCount = 0;
	private RandomAccessFile file;
	
	public void add(int docNumber, PatentResume resume) {
		totalWordCount += resume.getWordCount();
		try {
			byte[] bytes = resume.toByteArray();
			long offset = file.length();
			file.seek(offset);
			offsets.put(docNumber, offset);
			file.writeInt(bytes.length);
			file.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<Integer> getAllDocNumbers() {
		return new HashSet<Integer>(offsets.keys());
	}

	public int wordCount(Integer docNumber) {
		return get(docNumber).getWordCount();
	}

	public int getTotalWordCount() {
		return totalWordCount;
	}

	public PatentResume get(Integer docNumber) {
		try {
			long offset = offsets.get(docNumber);
			file.seek(offset);
			int length = file.readInt();
			byte[] bytes = new byte[length];
			file.read(bytes);
			return PatentResume.fromByteArray(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void write() throws IOException {
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		file.writeInt(totalWordCount);
		ObjectWriter.writeObject(offsets, OFFSETS_FILE);
	}

	@Override
	public void load() throws IOException {
		offsets = ObjectReader.<OffsetsIndex<Integer>>readObject(OFFSETS_FILE);
		file = new RandomAccessFile(PATENTS_FILE, "rw");
		file.seek(TOTAL_WORD_COUNT_OFFSET);
		this.totalWordCount = file.readInt();
	}

	@Override
	public void create() throws IOException {
		YahoogleUtils.deleteIfExists(PATENTS_FILE);
		YahoogleUtils.deleteIfExists(OFFSETS_FILE);
		file = new RandomAccessFile(PATENTS_FILE, "rw");
		file.writeInt(0); // totalWordCount
		offsets = new OffsetsIndex<Integer>();
	}

}
