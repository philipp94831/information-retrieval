package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.io.AbstractWriter;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.VByteWriter;

public class DocumentPosting implements Comparable<DocumentPosting> {

	private final int docNumber;
	private final Set<Posting> postings = new TreeSet<>();

	public DocumentPosting(int docNumber) {
		this.docNumber = docNumber;
	}

	public void add(Posting posting) {
		postings.add(posting);
	}

	@Override
	public int compareTo(DocumentPosting o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	public Set<Posting> getAll() {
		return postings;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public int size() {
		return postings.size();
	}

	public byte[] toByteArray() throws IOException {
		AbstractWriter positions = new VByteWriter();
		int oldPos = 0;
		for (Posting posting : postings) {
			int dp = (posting.getPosition() - oldPos);
			positions.writeInt(dp);
			oldPos = posting.getPosition();
		}
		byte[] encoded = positions.toByteArray();
		ByteWriter out = new ByteWriter();
		out.writeInt(docNumber); // docNumber
		out.writeInt(encoded.length); // size of block
		out.write(encoded);
		return out.toByteArray();
	}
}
