package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;

public class VByteReader implements AbstractReader {

	private final ByteBuffer in;

	public VByteReader(byte[] bytes) {
		in = ByteBuffer.wrap(bytes);
	}

	public VByteReader(byte[] bytes, int offset, int length) {
		in = ByteBuffer.wrap(bytes, offset, length);
	}

	@Override
	public boolean hasLeft() {
		return in.hasRemaining();
	}

	@Override
	public int readInt() {
		return (int) readLong();
	}

	@Override
	public long readLong() {
		long l = 0;
		while (true) {
			byte b = in.get();
			if ((b & 0xff) < 128) {
				l = 128 * l + b;
			} else {
				return (128 * l + ((b - 128) & 0xff));
			}
		}
	}

	@Override
	public short readShort() {
		return (short) readLong();
	}
}
