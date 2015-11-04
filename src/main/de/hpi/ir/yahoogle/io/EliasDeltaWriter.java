package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public class EliasDeltaWriter {
	
	private BitWriter out = new BitWriter();
	
	public void writeShort(short s) throws IOException {
		writeInt((int) s);
	}
	
	public void writeInt(int i) throws IOException {
		int len = 0;
		int lengthOfLen = 0;
		for (int temp = i; temp > 0; temp >>= 1) { // calculate 1+floor(log2(num))
			len++;
		}
		for (int temp = len; temp > 1; temp >>= 1) { // calculate floor(log2(len))
			lengthOfLen++;
		}
		for (int j = lengthOfLen; j > 0; --j) {
			out.write(0);
		}
		for (int j = lengthOfLen; j >= 0; --j) {
			out.write((len >> j) & 1);
		}
		for (int j = len - 2; j >= 0; j--) {
			out.write((i >> j) & 1);
		}
	}
	
	public byte[] toByteArray() {
		return out.toByteArray();
	}

}
