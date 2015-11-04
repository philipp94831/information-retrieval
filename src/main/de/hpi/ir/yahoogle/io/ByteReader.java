package de.hpi.ir.yahoogle.io;

import java.nio.ByteBuffer;

public class ByteReader {
	
	private ByteBuffer in;
	
	public ByteReader(byte[] bytes, int offset, int length) {
		in = ByteBuffer.wrap(bytes, offset, length);
	}
	
	public short readShort() {
		return in.getShort();
	}
	
	public int readInt() {
		return in.getInt();
	}

}
