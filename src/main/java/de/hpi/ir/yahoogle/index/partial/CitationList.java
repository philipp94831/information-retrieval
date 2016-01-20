package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.io.ByteWriter;

public class CitationList {
	
	private Set<Integer> citedFrom = new TreeSet<>();
	private int docNumber;

	public CitationList(int docNumber) {
		this.docNumber = docNumber;
	}

	public void add(int docNumber) {
		citedFrom.add(docNumber);
	}

	public byte[] toByteArray() throws IOException {
		ByteWriter block = new ByteWriter();
		for (Integer entry : citedFrom) {
			block.writeInt(entry);
		}
		return block.toByteArray();
	}

	public int getDocNumber() {
		return docNumber;
	}
}
