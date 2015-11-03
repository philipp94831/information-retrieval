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
		tmp_index.writeLong(YahoogleIndex.NO_NEXT_POSTING);
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
			BitWriter out = new BitWriter();
			short oldPos = 0;
			for (YahoogleIndexPosting posting : entry.getValue()) {
				encode(out, (short) (posting.getPosition() - oldPos));
				oldPos = posting.getPosition();
			}
			byte[] encoded = out.toByteArray();
			writeInt(temp, entry.getKey()); // docNumber
			writeShort(temp, (short) encoded.length); // size of block
			temp.write(encoded);
		}
		byte[] block = temp.toByteArray();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(Integer.BYTES + block.length);
		writeInt(bout, block.length);
		bout.write(temp.toByteArray());
		tmp_index.write(bout.toByteArray());
	}
	
	private static void encode(BitWriter out, short s) throws IOException {
		int len = 0;
		int lengthOfLen = 0;
		for (short temp = s; temp > 0; temp >>= 1) { // calculate 1+floor(log2(num))
			len++;
		}
		for (int temp = len; temp > 1; temp >>= 1) { // calculate floor(log2(len))
			lengthOfLen++;
		}
		for (int i = lengthOfLen; i > 0; --i) {
			out.write(0);
		}
		for (int i = lengthOfLen; i >= 0; --i) {
			out.write((len >> i) & 1);
		}
		for (int i = len - 2; i >= 0; i--) {
			out.write((s >> i) & 1);
		}
	}

	private static void writeInt(ByteArrayOutputStream bout, int value) throws IOException {
		bout.write(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
	}

	private static void writeShort(ByteArrayOutputStream bout, short value) throws IOException {
		bout.write(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
	}

}
