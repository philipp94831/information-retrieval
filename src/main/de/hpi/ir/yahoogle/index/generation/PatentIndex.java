package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class PatentIndex extends Loadable implements Iterable<PatentResume> {

	private static final String BASE_NAME = ".patents";
	private String name;
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

	public void delete() {
		deleteIfExists(fileName());
	}

	protected String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	@Override
	public Iterator<PatentResume> iterator() {
		return patents.iterator();
	}

	@Override
	public void load() throws IOException {
		patents = ObjectReader.readObject(fileName());
	}

	@Override
	public void write() throws IOException {
		ObjectWriter.writeObject(patents, fileName());
	}

}
