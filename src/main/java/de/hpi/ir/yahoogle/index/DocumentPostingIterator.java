package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

public class DocumentPostingIterator implements Iterator<DocumentPosting> {

	private static final Logger LOGGER = Logger
			.getLogger(DocumentPostingIterator.class.getName());
	private BinaryPostingList postingList;
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
		try {
			int size = postingList.getSize(read);
			read += Integer.BYTES;
			DocumentPosting result = postingList.next(read, size);
			read += size;
			return result;
		} catch (IOException e) {
			LOGGER.severe("Error retrieving next document posting");
		}
		return null;
	}
}
