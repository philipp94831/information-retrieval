package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public abstract class AbstractReader {
	
	public abstract int readInt() throws IOException;
	public abstract short readShort() throws IOException;
	public abstract boolean hasLeft();

}
