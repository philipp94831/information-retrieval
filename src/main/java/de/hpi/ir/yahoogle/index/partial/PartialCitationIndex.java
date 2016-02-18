package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Map.Entry;

import SearchEngine.SearchEngineYahoogle;

import java.util.TreeMap;

import de.hpi.ir.yahoogle.index.BinaryCitationList;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PartialCitationIndex extends Loadable
		implements Iterable<BinaryCitationList> {

	private static final String BASE_NAME = ".citations";
	private Map<Integer, CitationList> citations;
	private RandomAccessFile file;
	private final String name;

	public PartialCitationIndex(String name) {
		this.name = name;
	}

	public void add(int docNumber, int citedFrom) {
		if (citations.get(docNumber) == null) {
			citations.put(docNumber, new CitationList(docNumber));
		}
		citations.get(docNumber).add(citedFrom);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		citations = new TreeMap<>();
	}

	public long currentOffset() throws IOException {
		return file.getFilePointer();
	}

	public void delete() throws IOException {
		file.close();
		deleteIfExists(fileName());
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + name + BASE_NAME
				+ FILE_EXTENSION;
	}

	public long fileSize() throws IOException {
		return file.length();
	}

	@Override
	public CitationListIterator iterator() {
		return new CitationListIterator(this);
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
	}

	public BinaryCitationList read(long offset) throws IOException {
		file.seek(offset);
		byte[] info = new byte[Integer.BYTES * 2];
		file.read(info);
		ByteReader in = new ByteReader(info);
		int size = in.readInt();
		int docNumber = in.readInt();
		byte[] b = new byte[size];
		file.read(b);
		return new BinaryCitationList(docNumber, b);
	}

	@Override
	public void write() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		for (Entry<Integer, CitationList> entry : citations.entrySet()) {
			CitationList citationList = entry.getValue();
			byte[] bytes = citationList.toByteArray();
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.writeInt(citationList.getDocNumber());
			out.write(bytes);
			file.write(out.toByteArray());
		}
		file.close();
	}
}
