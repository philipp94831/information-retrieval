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

	private Map<Integer, List<YahoogleIndexPosting>> documentMap = new HashMap<Integer, List<YahoogleIndexPosting>>();

	public void add(int docNumber, YahoogleIndexPosting posting) {
		if (documentMap.get(docNumber) == null) {
			documentMap.put(docNumber, new ArrayList<YahoogleIndexPosting>());
		}
		documentMap.get(docNumber).add(posting);
	}

	public void write(String token, Long offset, RandomAccessFile tmp_index) throws IOException {
		long fileLength = tmp_index.length();
		if (offset != null) {
			tmp_index.seek(offset);
			tmp_index.writeLong(fileLength);
		}
		tmp_index.seek(fileLength);
		tmp_index.writeLong(YahoogleOnDiskIndex.NO_NEXT_POSTING);
		int totalSize = 0;
		for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
			totalSize += Integer.BYTES + Short.BYTES + entry.getValue().size() * YahoogleOnDiskIndex.POST_SIZE;
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream(totalSize);
		writeInt(bout, totalSize);
		for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
			writeInt(bout, entry.getKey()); // docNumber
			writeShort(bout, (short) entry.getValue().size()); // number of postings
			for (YahoogleIndexPosting posting : entry.getValue()) {
				writeShort(bout, posting.getPosition());
			}
		}
		tmp_index.write(bout.toByteArray());
	}

	private static void writeInt(ByteArrayOutputStream bout, int value) throws IOException {
		bout.write(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
	}

	private static void writeShort(ByteArrayOutputStream bout, short value) throws IOException {
		bout.write(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
	}

}
