package de.hpi.ir.yahoogle.index;

import java.util.Iterator;

class DocumentPostingIterator implements Iterator<DocumentPosting> {

	private final BinaryPostingList postingList;
	private int read = 0;

	public DocumentPostingIterator(BinaryPostingList binaryPostingList) {
		this.postingList = binaryPostingList;
	}

	@Override
	public boolean hasNext() {
		return read < postingList.size();
	}

	@Override
	public DocumentPosting next() {
		int size = postingList.getSize(read);
		read += Integer.BYTES;
		DocumentPosting result = postingList.next(read, size);
		read += size;
		return result;
	}
}
