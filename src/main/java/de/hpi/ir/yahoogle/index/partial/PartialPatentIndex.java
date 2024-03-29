package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.TreeSet;

import SearchEngine.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.FileUtils;

public class PartialPatentIndex extends Loadable
		implements Iterable<PatentResume> {

	private static final String BASE_NAME = ".patents";
	private RandomAccessFile file;
	private final String name;
	private Set<PatentResume> patents;

	public PartialPatentIndex(String name) {
		this.name = name;
	}

	public void add(PatentResume resume) {
		patents.add(resume);
	}

	@Override
	public void create() throws IOException {
		FileUtils.deleteIfExists(fileName());
		patents = new TreeSet<>();
	}

	public long currentOffset() throws IOException {
		return file.getFilePointer();
	}

	public void delete() throws IOException {
		file.close();
		FileUtils.deleteIfExists(fileName());
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + name + BASE_NAME
				+ FILE_EXTENSION;
	}

	public long fileSize() throws IOException {
		return file.length();
	}

	@Override
	public PatentResumeIterator iterator() {
		return new PatentResumeIterator(this);
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
	}

	public PatentResume read(long offset) throws IOException {
		file.seek(offset);
		int size = file.readInt();
		byte[] b = new byte[size];
		file.read(b);
		return new PatentResume(b);
	}

	@Override
	public void write() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		for (PatentResume resume : patents) {
			byte[] bytes = resume.toByteArray();
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.write(bytes);
			file.write(out.toByteArray());
		}
		file.close();
	}
}
