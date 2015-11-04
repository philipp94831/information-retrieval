package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public class EliasDeltaReader {
	
	private BitReader in;

	public EliasDeltaReader(byte[] bytes) {
		in = new BitReader(bytes);
	}
	
	public boolean hasLeft() {
		return in.hasLeft();
	}
	
	public short readShort() throws IOException {
		return (short) readInt();
	}
	
	public int readInt() throws IOException {
		int num = 1;
		int len = 1;
		int lengthOfLen = 0;
		while (!in.read()) {// potentially dangerous with malformed files.
			lengthOfLen++;
		}
		for (int i = 0; i < lengthOfLen; i++) {
			len <<= 1;
			if (in.read()) {
				len |= 1;
			}
		}
		for (int i = 0; i < len - 1; i++) {
			num <<= 1;
			if (in.read()) {
				num |= 1;
			}
		}
		return num;
	}

}