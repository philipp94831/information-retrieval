package de.hpi.ir.yahoogle.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VByteWriter implements AbstractWriter {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Override
	public byte[] toByteArray() {
		return out.toByteArray();
	}

	@Override
	public void writeInt(int i) throws IOException {
		writeLong(i);
	}

	@Override
	public void writeLong(long l) throws IOException {
		byte[] bytes;
		if (l == 0) {
			bytes = new byte[] { 0 };
		} else {
			int i = (int) (Math.log(l) / Math.log(128)) + 1;
			bytes = new byte[i];
			int j = i - 1;
			do {
				bytes[j--] = (byte) (l % 128);
				l /= 128;
			} while (j >= 0);
			bytes[i - 1] += 128;
		}
		for (byte b : bytes) {
			out.write(b);
		}
	}

	@Override
	public void writeShort(short s) throws IOException {
		writeLong(s);
	}
}
