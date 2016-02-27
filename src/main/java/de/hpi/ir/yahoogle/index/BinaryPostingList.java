package de.hpi.ir.yahoogle.index;

import java.util.Iterator;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.util.merge.Mergeable;

public class BinaryPostingList
		implements Iterable<DocumentPosting>, Mergeable<String> {

	private final byte[] bytes;
	private String token;

	public BinaryPostingList(byte[] bytes) {
		this.bytes = bytes;
	}

	public BinaryPostingList(String token, byte[] bytes) {
		this.token = token;
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public String getKey() {
		return token;
	}

	public int getSize(int offset) {
		ByteReader in = new ByteReader(bytes, offset, Integer.BYTES);
		return in.readInt();
	}

	public String getToken() {
		return token;
	}

	@Override
	public Iterator<DocumentPosting> iterator() {
		return new DocumentPostingIterator(this);
	}

	public DocumentPosting next(int offset, int size) {
		ByteReader in = new ByteReader(bytes, offset, size);
		byte[] bytes = in.read(size);
		return DocumentPosting.fromBytes(bytes);
	}

	public int size() {
		return bytes.length;
	}
}
