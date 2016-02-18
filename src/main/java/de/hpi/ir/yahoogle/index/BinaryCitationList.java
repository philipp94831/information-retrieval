package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.Mergeable;

public class BinaryCitationList
		implements Comparable<BinaryCitationList>, Mergeable<Integer> {

	private byte[] bytes;
	private final int docNumber;

	public BinaryCitationList(int docNumber, byte[] b) {
		this.docNumber = docNumber;
		this.bytes = b;
	}

	public void append(BinaryCitationList postingList) throws IOException {
		ByteWriter out = new ByteWriter();
		out.write(bytes);
		out.write(postingList.getBytes());
		bytes = out.toByteArray();
	}

	@Override
	public int compareTo(BinaryCitationList o) {
		int comp = Integer.compare(docNumber, o.docNumber);
		return comp == 0 ? 1 : comp;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public List<Integer> getDocNumbers() {
		ByteReader in = new ByteReader(bytes);
		List<Integer> result = new ArrayList<>();
		while (in.hasLeft()) {
			result.add(in.readInt());
		}
		return result;
	}

	@Override
	public Integer getKey() {
		return this.docNumber;
	}
}
