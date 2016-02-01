package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.VByteReader;

public class BinaryPostingList
		implements Comparable<BinaryPostingList>, Iterable<DocumentPosting> {

	private static final int MAX_SIZE = 16 * 1024;
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

	public byte[] append(byte[] newBytes) throws IOException {
		ByteWriter out = new ByteWriter(MAX_SIZE);
		boolean written = false;
		ByteReader in1 = new ByteReader(bytes);
		ByteReader in2 = new ByteReader(newBytes);
		BinaryDocumentPosting next1 = null;
		BinaryDocumentPosting next2 = null;
		while (in1.hasLeft() || in2.hasLeft() || next1 != null
				|| next2 != null) {
			if (next1 == null && in1.hasLeft()) {
				next1 = new BinaryDocumentPosting(in1);
			}
			if (next2 == null && in2.hasLeft()) {
				next2 = new BinaryDocumentPosting(in2);
			}
			byte[] toWrite;
			if (next2 == null || next1 != null && next1.compareTo(next2) < 0) {
				toWrite = next1.toByteArray();
				next1 = null;
			} else {
				toWrite = next2.toByteArray();
				next2 = null;
			}
			if(toWrite.length <= out.spaceLeft()) {
				out.write(toWrite);
			} else {
				bytes = out.toByteArray();
				written = true;
				out = new ByteWriter();
			}
		}
		if(!written) {
			bytes = out.toByteArray();
			return new byte[0];
		} else {
			return out.toByteArray();
		}
	}

	@Override
	public int compareTo(BinaryPostingList o) {
		if (token != null) {
			int comp = token.compareTo(o.token);
			return comp == 0 ? 1 : comp;
		}
		return -1;
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
			Posting posting = new Posting();
			posting.setPosition(oldPos + p);
			document.add(posting);
			oldPos += p;
		}
		read += bsize;
		return document;
	}

	public boolean hasLeft() {
		return read < bytes.length;
	}
}
