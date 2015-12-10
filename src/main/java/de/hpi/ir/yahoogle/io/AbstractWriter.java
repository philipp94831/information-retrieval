package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public interface AbstractWriter {

	byte[] toByteArray();

	void writeInt(int i) throws IOException;

	void writeLong(long l) throws IOException;

	void writeShort(short s) throws IOException;
}