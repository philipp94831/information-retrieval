package de.hpi.ir.yahoogle;

import java.io.IOException;
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

	public byte[] toByteArray() throws IOException {
		ByteWriter block = new ByteWriter();
		for (Entry<Integer, List<YahoogleIndexPosting>> entry : documentMap.entrySet()) {
			AbstractWriter positions = new EliasDeltaWriter();
			int oldPos = 0;
			for (YahoogleIndexPosting posting : entry.getValue()) {
				short dp = (short) (posting.getPosition() - oldPos);
				positions.writeShort(dp);
				oldPos = posting.getPosition();
			}
			byte[] encoded = positions.toByteArray();
			block.writeInt(entry.getKey()); // docNumber
			block.writeShort((short) encoded.length); // size of block
			block.write(encoded);
		}
		return block.toByteArray();
	}

}
