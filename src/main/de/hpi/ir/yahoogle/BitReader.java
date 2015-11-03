package de.hpi.ir.yahoogle;

import java.util.BitSet;

public class BitReader {

	private BitSet in;
	private int pos = 0;	
	
	public BitReader(byte[] bytes) {
		in = BitSet.valueOf(bytes);
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
