package de.hpi.ir.yahoogle.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteWriter implements AbstractWriter {

	private final ByteArrayOutputStream out;
	private int size = Integer.MAX_VALUE;

	public ByteWriter() {
		out = new ByteArrayOutputStream();
	}

	public ByteWriter(int size) {
		this.size = size;
		out = new ByteArrayOutputStream(size);
	}

	public int spaceLeft() {
		return size - out.size();
	}

	@Override
	public byte[] toByteArray() {
		return out.toByteArray();
	}

	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}

	public void writeDouble(double d) throws IOException {
		out.write(ByteBuffer.allocate(Double.BYTES).putDouble(d).array());
	}

	@Override
	public void writeInt(int i) throws IOException {
		out.write(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
	}

	@Override
	public void writeLong(long l) throws IOException {
		out.write(ByteBuffer.allocate(Long.BYTES).putLong(l).array());
	}

	@Override
	public void writeShort(short s) throws IOException {
		out.write(ByteBuffer.allocate(Short.BYTES).putShort(s).array());
	}

	public void writeUTF(String s) throws IOException {
		writeShort((short) s.length());
		out.write(s.getBytes(Charset.forName("UTF-8")));
	}
}