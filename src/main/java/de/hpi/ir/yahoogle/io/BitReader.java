package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;
import java.util.BitSet;

class BitReader {

	private final BitSet in;
	private int pos = 0;

	public BitReader(byte[] bytes) {
		in = BitSet.valueOf(bytes);
	}

	public BitReader(byte[] b, int offset, int length) {
		in = BitSet.valueOf(ByteBuffer.wrap(b, offset, length));
	}

	public boolean hasLeft() {
		return pos < in.length();
	}

	public boolean read() {
		boolean result = in.get(pos);
		pos++;
		return result;
	}
}
