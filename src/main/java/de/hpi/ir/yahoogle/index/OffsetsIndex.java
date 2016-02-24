package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import SearchEngine.SearchEngineYahoogle;

import java.util.TreeMap;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;
import de.hpi.ir.yahoogle.util.FileUtils;

public abstract class OffsetsIndex<K> extends Loadable {

	private static final String BASE_NAME = ".offsets";
	private static final int BLOCK_SIZE = 16 * 1024 - Integer.BYTES;
	private static final String SKIPLIST_BASE_NAME = ".skiplist" + BASE_NAME;
	private ByteWriter currentBlock;
	private int currentBlockSize;
	RandomAccessFile file;
	private final String name;
	TreeMap<K, Long> skiplist;

	OffsetsIndex(String name) {
		this.name = name;
		createNewBlock();
	}

	@Override
	public void create() throws IOException {
		FileUtils.deleteIfExists(fileName());
		FileUtils.deleteIfExists(skipListFileName());
		skiplist = new TreeMap<>();
		file = new RandomAccessFile(fileName(), "rw");
	}

	private void createNewBlock() {
		currentBlock = new ByteWriter(BLOCK_SIZE);
		currentBlockSize = 0;
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + name + BASE_NAME
				+ FILE_EXTENSION;
	}

	public Long get(K key) throws IOException {
		Entry<K, Long> entry = skiplist.floorEntry(key);
		file.seek(entry.getValue());
		int size = file.readInt();
		byte[] b = new byte[size];
		file.read(b);
		ByteReader in = new ByteReader(b);
		while (in.hasLeft()) {
			K newKey = readKey(in);
			long offset = in.readLong();
			if (newKey.equals(key)) {
				return offset;
			}
		}
		return null;
	}

	public List<K> keys() throws IOException {
		List<K> keys = new ArrayList<>();
		for (Long offset : skiplist.values()) {
			file.seek(offset);
			int size = file.readInt();
			byte[] b = new byte[size];
			file.read(b);
			ByteReader in = new ByteReader(b);
			while (in.hasLeft()) {
				K key = readKey(in);
				in.readLong();
				keys.add(key);
			}
		}
		return keys;
	}

	@Override
	public void load() throws IOException {
		skiplist = ObjectReader
				.<TreeMap<K, Long>> readObject(skipListFileName());
		file = new RandomAccessFile(fileName(), "rw");
	}

	public void put(K key, long offset) throws IOException {
		ByteWriter out = new ByteWriter();
		writeKey(key, out);
		out.writeLong(offset);
		byte[] b = out.toByteArray();
		if (currentBlockSize + b.length > BLOCK_SIZE) {
			writeBlock();
		}
		if (currentBlockSize == 0) {
			long skipOffset = file.getFilePointer();
			skiplist.put(key, skipOffset);
		}
		currentBlock.write(b);
		currentBlockSize += b.length;
	}

	protected abstract K readKey(ByteReader in);

	private String skipListFileName() {
		return SearchEngineYahoogle.getTeamDirectory() + name
				+ SKIPLIST_BASE_NAME + FILE_EXTENSION;
	}

	@Override
	public void write() throws IOException {
		writeBlock();
		ObjectWriter.writeObject(skiplist, skipListFileName());
		file.close();
	}

	private void writeBlock() throws IOException {
		byte[] bytes = currentBlock.toByteArray();
		ByteWriter out = new ByteWriter();
		out.writeInt(bytes.length);
		out.write(bytes);
		file.write(out.toByteArray());
		createNewBlock();
	}

	protected abstract void writeKey(K key, ByteWriter out) throws IOException;
}
