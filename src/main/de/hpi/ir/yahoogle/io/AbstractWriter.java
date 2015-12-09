package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public interface AbstractWriter {

	public abstract byte[] toByteArray();

	public abstract void writeInt(int i) throws IOException;

	public abstract void writeLong(long l) throws IOException;

	public abstract void writeShort(short s) throws IOException;
}