package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteReader implements AbstractReader {

	private ByteBuffer in;

	public ByteReader(byte[] b) {
		in = ByteBuffer.wrap(b);
	}

	public ByteReader(byte[] bytes, int offset, int length) {
		in = ByteBuffer.wrap(bytes, offset, length);
	}

	@Override
	public boolean hasLeft() {
		return in.hasRemaining();
	}

	@Override
	public int readInt() {
		return in.getInt();
	}

	@Override
	public long readLong() {
		return in.getLong();
	}

	@Override
	public short readShort() {
		return in.getShort();
	}

	public String readUTF() {
		int length = Short.toUnsignedInt(in.getShort());
		byte[] b = new byte[length];
		in.get(b);
		return new String(b, Charset.forName("UTF-8"));
	}
}