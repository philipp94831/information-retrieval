package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public abstract class AbstractReader {

	public AbstractReader(byte[] b, int i, int bsize) {
	}
	
	public abstract int readInt() throws IOException;
	public abstract short readShort() throws IOException;
	public abstract boolean hasLeft();

}
