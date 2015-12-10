package de.hpi.ir.yahoogle.io;

import java.io.IOException;

public interface AbstractReader {

	boolean hasLeft();

	int readInt() throws IOException;

	long readLong() throws IOException;

	short readShort() throws IOException;
}