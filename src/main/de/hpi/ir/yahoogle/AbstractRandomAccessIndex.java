package de.hpi.ir.yahoogle;

import java.io.RandomAccessFile;

public abstract class AbstractRandomAccessIndex {

	protected RandomAccessFile index;
	protected OffsetsIndex offsets;

}
