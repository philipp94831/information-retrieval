package de.hpi.ir.yahoogle.io;

public class EliasDeltaReader implements AbstractReader {

	private final BitReader in;

	public EliasDeltaReader(byte[] bytes, int offset, int length) {
		in = new BitReader(bytes, offset, length);
	}

	@Override
	public boolean hasLeft() {
		return in.hasLeft();
	}

	@Override
	public int readInt() {
		return (int) readLong();
	}

	@Override
	public long readLong() {
		long num = 1;
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

	@Override
	public short readShort() {
		return (short) readLong();
	}
}