package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class StringOffsetsIndex extends OffsetsIndex<String> {

	public StringOffsetsIndex(String name) {
		super(name);
	}

	public List<String> getKeysForPrefix(String prefix) throws IOException {
		Entry<String, Long> entry = skiplist.floorEntry(prefix);
		file.seek(entry.getValue());
		List<String> keys = new ArrayList<>();
		while (true) {
			int size = file.readInt();
			byte[] b = new byte[size];
			file.read(b);
			ByteReader in = new ByteReader(b);
			while (in.hasLeft()) {
				String newKey = readKey(in);
				in.readLong();
				int comp = newKey
						.substring(0,
								Math.min(prefix.length(), newKey.length()))
						.compareTo(prefix);
				switch (Integer.signum(comp)) {
				case 0:
					keys.add(newKey);
					break;
				case 1:
					return keys;
				default:
					break;
				}
			}
		}
	}

	@Override
	protected String readKey(ByteReader in) {
		return in.readUTF();
	}

	@Override
	protected void writeKey(String key, ByteWriter out) throws IOException {
		out.writeUTF(key);
	}
}
