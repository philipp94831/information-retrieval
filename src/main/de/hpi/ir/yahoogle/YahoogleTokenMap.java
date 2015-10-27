package de.hpi.ir.yahoogle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class YahoogleTokenMap {

	private static final long NO_NEXT_POSTING = -1;

	private Map<Integer, List<YahoogleIndexPosting>> documentMap = new HashMap<Integer, List<YahoogleIndexPosting>>();

	public void add(int docNumber, YahoogleIndexPosting posting) {
		if (documentMap.get(docNumber) == null) {
			documentMap.put(docNumber, new ArrayList<YahoogleIndexPosting>());
		}
		documentMap.get(docNumber).add(posting);
	}

	public void write(String token, Long offset, RandomAccessFile tmp_index) {
		try {
			if (offset == null) {
				offset = tmp_index.length();
			} else {
				tmp_index.seek(offset);
				Long next;
				while ((next = tmp_index.readLong()) != NO_NEXT_POSTING) {
					tmp_index.seek(next);
					offset = next;
				}
				tmp_index.seek(offset);
				tmp_index.writeLong(tmp_index.length());
			}
			tmp_index.seek(tmp_index.length());
			tmp_index.writeLong(NO_NEXT_POSTING);

			int totalSize = 0;
			for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
				totalSize += Integer.BYTES + Short.BYTES + entry.getValue().size() * Short.BYTES;
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream(totalSize);
			writeInt(bout, totalSize);
			for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
				writeInt(bout, entry.getKey()); //docNumber
				writeShort(bout, (short) entry.getValue().size()); //number of postings
				for (YahoogleIndexPosting posting : entry.getValue()) {
					writeShort(bout, posting.getPosition());
				}
			}
			tmp_index.write(bout.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeInt(ByteArrayOutputStream bout, int value) throws IOException {
			bout.write(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
	}

	private void writeShort(ByteArrayOutputStream bout, short value) throws IOException {
			bout.write(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
	}

}
