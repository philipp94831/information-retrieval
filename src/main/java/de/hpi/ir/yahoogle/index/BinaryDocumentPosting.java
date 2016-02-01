package de.hpi.ir.yahoogle.index;

import java.io.IOException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class BinaryDocumentPosting implements Comparable<BinaryDocumentPosting> {

	private final byte[] postings;
	private final int docNumber;

	public BinaryDocumentPosting(ByteReader in) {
		int docNumber = in.readInt();
		int length = in.readInt();
		postings = in.read(length);
		this.docNumber = docNumber;
	}

	@Override
	public int compareTo(BinaryDocumentPosting o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	public byte[] toByteArray() throws IOException {
		ByteWriter out = new ByteWriter();
		out.writeInt(docNumber); // docNumber
		out.writeInt(postings.length); // size of block
		out.write(postings);
		return out.toByteArray();
	}
}
