package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.BinaryPostingList;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PartialTokenDictionary extends Loadable implements Iterable<BinaryPostingList> {

	private static final String BASE_NAME = ".dictionary";
	private Map<String, PostingList> dictionary;
	private RandomAccessFile file;
	private String name;

	public PartialTokenDictionary(String name) {
		this.name = name;
	}

	public void add(String token, int docNumber, Posting posting) {
		if (dictionary.get(token) == null) {
			dictionary.put(token, new PostingList(token));
		}
		dictionary.get(token).add(docNumber, posting);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		dictionary = new TreeMap<String, PostingList>();
	}

	public long currentOffset() throws IOException {
		return file.getFilePointer();
	}

	public void delete() throws IOException {
		file.close();
		deleteIfExists(fileName());
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	public long fileSize() throws IOException {
		return file.length();
	}

	public Map<String, PostingList> getDict() {
		return dictionary;
	}

	@Override
	public PostingListIterator iterator() {
		return new PostingListIterator(this);
	}

	@Override
	public void load() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
	}

	public BinaryPostingList read(long offset) throws IOException {
		file.seek(offset);
		int size = file.readInt();
		String token = file.readUTF();
		byte[] b = new byte[size];
		file.read(b);
		return new BinaryPostingList(token, b);
	}

	@Override
	public void write() throws IOException {
		file = new RandomAccessFile(fileName(), "rw");
		for (Entry<String, PostingList> entry : dictionary.entrySet()) {
			PostingList postingList = entry.getValue();
			byte[] bytes = postingList.toByteArray();
			ByteWriter out = new ByteWriter();
			out.writeInt(bytes.length);
			out.writeUTF(postingList.getToken());
			out.write(bytes);
			file.write(out.toByteArray());
		}
		file.close();
	}
}
