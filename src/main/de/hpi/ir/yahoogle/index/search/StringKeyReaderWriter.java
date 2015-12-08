package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class StringKeyReaderWriter extends KeyReaderWriter<String> {

	@Override
	public String readKey(ByteReader in) {
		return in.readUTF();
	}

	@Override
	public void writeKey(String key, ByteWriter out) throws IOException {
		out.writeUTF(key);
	}

}
