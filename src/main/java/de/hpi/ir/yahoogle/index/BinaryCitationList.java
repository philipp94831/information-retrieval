package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class BinaryCitationList implements Comparable<BinaryCitationList> {

	private byte[] bytes;
	private final int docNumber;

	public BinaryCitationList(int docNumber, byte[] b) {
		this.docNumber = docNumber;
		this.bytes = b;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getDocNumber() {
		return docNumber;
	}

	@Override
	public int compareTo(BinaryCitationList o) {
		int comp = Integer.compare(docNumber, o.docNumber);
		return comp == 0 ? 1 : comp;
	}

	public void append(byte[] newBytes) throws IOException {
		ByteWriter out = new ByteWriter();
		out.write(bytes);
		out.write(newBytes);
		bytes = out.toByteArray();
	}

	public List<Integer> getDocNumbers() {
		ByteReader in = new ByteReader(bytes);
		List<Integer> result = new ArrayList<>();
		while(in.hasLeft()) {
			result.add(in.readInt());
		}
		return result;
	}
}
