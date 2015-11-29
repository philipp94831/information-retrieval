
package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public interface AbstractReader {

	public abstract boolean hasLeft();

	public abstract int readInt() throws IOException;

	public abstract long readLong() throws IOException;

	public abstract short readShort() throws IOException;

}