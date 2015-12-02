package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.YahoogleUtils;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class LinkedRandomAccessIndex extends AbstractRandomAccessIndex {

	private static final long NO_NEXT_POSTING = -1;
	private static final String POSTINGS_FILE = SearchEngineYahoogle.getTeamDirectory() + "/tmp.postings.yahoogle";

	public static LinkedRandomAccessIndex create() {
		YahoogleUtils.deleteIfExists(POSTINGS_FILE);
		return new LinkedRandomAccessIndex();
	}

	private OffsetsIndex<String> lastOffsets = new OffsetsIndex<String>();

	public LinkedRandomAccessIndex() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		offsets = new OffsetsIndex<String>();
	}

	public void add(String token, TokenIndexBuffer value) throws IOException {
		long fileLength = index.length();
		if (offsets.get(token) == null) {
			offsets.put(token, fileLength);
		}
		Long offset = lastOffsets.get(token);
		if (offset != null) {
			index.seek(offset);
			index.writeLong(fileLength);
		}
		lastOffsets.put(token, fileLength);
		byte[] block = value.toByteArray();
		ByteWriter bout = new ByteWriter(Long.BYTES + Integer.BYTES + block.length);
		bout.writeLong(NO_NEXT_POSTING);
		bout.writeInt(block.length);
		bout.write(block);
		index.seek(fileLength);
		index.write(bout.toByteArray());
	}

	public OrganizedRandomAccessIndex reorganize() {
		OrganizedRandomAccessIndex newIndex = OrganizedRandomAccessIndex.create();
		try {
			for (Entry<String, Long> entry : offsets.entrySet()) {
				long offset = entry.getValue();
				long next = offset;
				ByteWriter bout = new ByteWriter();
				while (next != NO_NEXT_POSTING) {
					index.seek(offset);
					next = index.readLong();
					int size = index.readInt();
					byte[] b = new byte[size];
					index.readFully(b);
					bout.write(b);
				}
				newIndex.add(entry.getKey(), bout.toByteArray());
			}
			index.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		YahoogleUtils.deleteIfExists(POSTINGS_FILE);
		return newIndex;
	}

}
