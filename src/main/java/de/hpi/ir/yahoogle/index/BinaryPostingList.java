package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.VByteReader;
import de.hpi.ir.yahoogle.util.Mergeable;

public class BinaryPostingList
		implements Iterable<DocumentPosting>, Mergeable<String> {

	private byte[] bytes;
	private String token;
	private int read;

	public BinaryPostingList(String token, byte[] bytes) {
		this.token = token;
		this.bytes = bytes;
	}

	public BinaryPostingList(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getToken() {
		return token;
	}

	@Override
	public Iterator<DocumentPosting> iterator() {
		read = 0;
		return new DocumentPostingIterator(this);
	}

	public DocumentPosting next() throws IOException {
		AbstractReader in = new ByteReader(bytes, read, 2 * Integer.BYTES);
		read += 2 * Integer.BYTES;
		int docNumber = in.readInt();
		int bsize = in.readInt();
		in = new VByteReader(bytes, read, bsize);
		DocumentPosting document = new DocumentPosting(docNumber);
		int oldPos = 0;
		while (in.hasLeft()) {
			int p = in.readInt();
			document.add(oldPos + p);
			oldPos += p;
		}
		read += bsize;
		return document;
	}

	public boolean hasLeft() {
		return read < bytes.length;
	}

	@Override
	public String getKey() {
		return token;
	}
}
