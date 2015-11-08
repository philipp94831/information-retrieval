package de.hpi.ir.yahoogle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.io.AbstractWriter;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.EliasDeltaWriter;

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
		ByteWriter temp = new ByteWriter();
		for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
			AbstractWriter out = new EliasDeltaWriter();
			int oldPos = 0;
			for (YahoogleIndexPosting posting : entry.getValue()) {
				short dp = (short) (posting.getPosition() - oldPos);
				out.writeShort(dp);
				oldPos = posting.getPosition();
			}
			byte[] encoded = out.toByteArray();
			temp.writeInt(entry.getKey()); // docNumber
			temp.writeShort((short) encoded.length); // size of block
			temp.write(encoded);
		}
		byte[] block = temp.toByteArray();
		ByteWriter bout = new ByteWriter(Integer.BYTES + block.length);
		bout.writeInt(block.length);
		bout.write(temp.toByteArray());
		tmp_index.write(bout.toByteArray());
	}

}
