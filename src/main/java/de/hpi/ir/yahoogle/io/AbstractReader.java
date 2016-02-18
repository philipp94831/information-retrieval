package de.hpi.ir.yahoogle.io;

public interface AbstractReader {

	boolean hasLeft();

	int readInt();

	long readLong();

	short readShort();
}