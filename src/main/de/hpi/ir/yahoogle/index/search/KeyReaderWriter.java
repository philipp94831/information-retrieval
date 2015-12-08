package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public abstract class KeyReaderWriter<T> {

	public abstract T readKey(ByteReader in);

	public abstract void writeKey(T key, ByteWriter out) throws IOException;

}
