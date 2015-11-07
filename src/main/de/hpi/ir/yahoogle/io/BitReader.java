package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class BitReader {

	private BitSet in;
	private int pos = 0;
	
	public BitReader(byte[] bytes) {
		in = BitSet.valueOf(bytes);
	}

	public BitReader(byte[] b, int i, short bsize) {
		in = BitSet.valueOf(ByteBuffer.wrap(b, i, bsize));
	}

	public boolean read() {
		boolean result = in.get(pos);
		pos++;
		return result;
	}

	public boolean hasLeft() {
		return pos < in.length();
	}
	
}
