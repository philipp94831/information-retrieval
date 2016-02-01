package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.io.AbstractWriter;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.VByteWriter;
import de.hpi.ir.yahoogle.util.Mergeable;

public class DocumentPosting implements Comparable<DocumentPosting>, Mergeable<Integer> {

	private final int docNumber;
	private final Set<Integer> positions = new TreeSet<>();

	public DocumentPosting(int docNumber) {
		this.docNumber = docNumber;
	}

	public void add(Integer position) {
		positions.add(position);
	}

	@Override
	public int compareTo(DocumentPosting o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	public Set<Integer> getAll() {
		return positions;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public int size() {
		return positions.size();
	}

	public byte[] toByteArray() throws IOException {
		AbstractWriter pout = new VByteWriter();
		int oldPos = 0;
		for (Integer posting : positions) {
			int dp = (posting - oldPos);
			pout.writeInt(dp);
			oldPos = posting;
		}
		byte[] encoded = pout.toByteArray();
		ByteWriter out = new ByteWriter();
		out.writeInt(docNumber); // docNumber
		out.writeInt(encoded.length); // size of block
		out.write(encoded);
		return out.toByteArray();
	}

	@Override
	public Integer getKey() {
		return docNumber;
	}

	public void addAll(DocumentPosting document) {
		positions.addAll(document.getAll());
	}

	public void merge(DocumentPosting next, int delta) {
		if (docNumber != next.docNumber) {
			throw new RuntimeException("Trying to merge different Documents");
		}
		Set<Integer> newPos = next.getAll();
		Set<Integer> oldPos = positions;
		oldPos.retainAll(newPos.stream().map(p -> p - delta).collect(Collectors.toSet()));
	}
}
