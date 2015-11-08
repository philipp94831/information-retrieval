package de.hpi.ir.yahoogle.index;

import java.io.RandomAccessFile;

public abstract class AbstractRandomAccessIndex {

	protected RandomAccessFile index;
	protected OffsetsIndex offsets;

}
