package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;

public class ByteReader implements AbstractReader {
	
	private ByteBuffer in;
	
	public ByteReader(byte[] bytes, int offset, int length) {
		in = ByteBuffer.wrap(bytes, offset, length);
	}
	
	@Override
	public short readShort() {
		return in.getShort();
	}
	
	@Override
	public int readInt() {
		return in.getInt();
	}

	@Override
	public boolean hasLeft() {
		return in.hasRemaining();
	}

}
