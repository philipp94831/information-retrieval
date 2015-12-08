package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentIndex extends Loadable implements Iterable<PatentResume> {

	private static final String BASE_NAME = ".patents";
	private String name;
	private RandomAccessFile file;
	private Set<PatentResume> patents;

	public PatentIndex(String name) {
		this.name = name;
	}

	public void add(PatentResume resume) {
		patents.add(resume);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		patents = new TreeSet<PatentResume>();
	}

	public void delete() throws IOException {
		file.close();
		deleteIfExists(fileName());
	}

	protected String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	@Override
	public PatentResumeIterator iterator() {
		return new PatentResumeIterator(this);
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
	}

	@Override
	public void write() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		for(PatentResume resume: patents) {
			byte[] bytes = resume.toByteArray();
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.writeInt(resume.getDocNumber());
			out.write(bytes);
			file.write(out.toByteArray());
		}
		file.close();
	}
	
	public long currentOffset() throws IOException {
		return file.getFilePointer();
	}
	
	public PatentResume read(long offset) throws IOException {
		file.seek(offset);
		int size = file.readInt();
		int docNumber = file.readInt();
		byte[] b = new byte[size];
		file.read(b);
		return PatentResume.fromByteArray(docNumber, b);
	}
	
	public long fileSize() throws IOException {
		return file.length();
	}

}
