package de.hpi.ir.yahoogle.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteWriter {
	
	private ByteArrayOutputStream out;
	
	public ByteWriter() {
		out = new ByteArrayOutputStream();
	}
	
	public ByteWriter(int size) {
		out = new ByteArrayOutputStream(size);
	}

	public void writeInt(int i) throws IOException {
		out.write(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
	}

	public void writeShort(short s) throws IOException {
		out.write(ByteBuffer.allocate(Short.BYTES).putShort(s).array());
	}
	
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}
	
	public byte[] toByteArray() {
		return out.toByteArray();
	}

}
