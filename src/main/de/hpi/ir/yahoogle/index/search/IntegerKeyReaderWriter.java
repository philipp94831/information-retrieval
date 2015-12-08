package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class IntegerKeyReaderWriter extends KeyReaderWriter<Integer> {

	@Override
	public Integer readKey(ByteReader in) {
		return in.readInt();
	}

	@Override
	public void writeKey(Integer key, ByteWriter out) throws IOException {
		out.writeInt(key);
	}

}
