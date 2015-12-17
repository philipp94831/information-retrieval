package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import de.hpi.ir.yahoogle.io.AbstractWriter;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.EliasDeltaWriter;

class PostingList implements Comparable<PostingList>, Iterable<DocumentPosting> {

	private final TreeMap<Integer, DocumentPosting> documents = new TreeMap<>();
	private final String token;

	public PostingList(String token) {
		this.token = token;
	}

	public void add(DocumentPosting posting) {
		documents.put(posting.getDocNumber(), posting);
	}

	public void add(int docNumber, Posting posting) {
		if (documents.get(docNumber) == null) {
			documents.put(docNumber, new DocumentPosting(docNumber));
		}
		documents.get(docNumber).add(posting);
	}

	@Override
	public int compareTo(PostingList o) {
		int comp = token.compareTo(o.token);
		if (comp == 0) {
			return documents.firstKey().compareTo(o.documents.firstKey());
		} else {
			return comp;
		}
	}

	public DocumentPosting get(int i) {
		return documents.get(i);
	}

	public String getToken() {
		return token;
	}

	@Override
	public Iterator<DocumentPosting> iterator() {
		return documents.values().iterator();
	}

	public byte[] toByteArray() throws IOException {
		ByteWriter block = new ByteWriter();
		for (DocumentPosting entry : documents.values()) {
			AbstractWriter positions = new EliasDeltaWriter();
			int oldPos = 0;
			for (Posting posting : entry) {
				short dp = (short) (posting.getPosition() - oldPos);
				positions.writeShort(dp);
				oldPos = posting.getPosition();
			}
			byte[] encoded = positions.toByteArray();
			block.writeInt(entry.getDocNumber()); // docNumber
			block.writeShort((short) encoded.length); // size of block
			block.write(encoded);
		}
		return block.toByteArray();
	}
}
