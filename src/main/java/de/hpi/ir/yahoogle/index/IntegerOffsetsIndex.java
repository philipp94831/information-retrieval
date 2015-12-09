package de.hpi.ir.yahoogle.index;

import java.io.IOException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class IntegerOffsetsIndex extends OffsetsIndex<Integer> {

	public IntegerOffsetsIndex(String name) {
		super(name);
	}

	@Override
	protected Integer readKey(ByteReader in) {
		return in.readInt();
	}

	@Override
	protected void writeKey(Integer key, ByteWriter out) throws IOException {
		out.writeInt(key);
	}
}
