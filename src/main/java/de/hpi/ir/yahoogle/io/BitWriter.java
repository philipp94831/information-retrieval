package de.hpi.ir.yahoogle.io;

import java.util.BitSet;

public class BitWriter {

	private final BitSet out = new BitSet();
	private int pos = 0;

	public byte[] toByteArray() {
		return out.toByteArray();
	}

	public void write(boolean bit) {
		out.set(pos, bit);
		pos++;
	}

	public void write(long bit) {
		write(bit != 0);
	}
}
