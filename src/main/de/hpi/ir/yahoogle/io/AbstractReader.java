package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public interface AbstractReader {
	
	public abstract int readInt() throws IOException;
	public abstract short readShort() throws IOException;
	public abstract boolean hasLeft();

}
