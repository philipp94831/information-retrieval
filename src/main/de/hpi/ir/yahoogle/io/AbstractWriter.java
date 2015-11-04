package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public abstract class AbstractWriter {
	
	public abstract void writeShort(short s) throws IOException;
	public abstract void writeInt(int i) throws IOException;
	public abstract byte[] toByteArray();

}
