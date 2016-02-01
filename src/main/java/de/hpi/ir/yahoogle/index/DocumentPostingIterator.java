package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;


public class DocumentPostingIterator implements Iterator<DocumentPosting> {

	private static final Logger LOGGER = Logger
			.getLogger(DocumentPostingIterator.class.getName());
	private BinaryPostingList postingList;

	public DocumentPostingIterator(BinaryPostingList binaryPostingList) {
		this.postingList = binaryPostingList;
	}

	@Override
	public boolean hasNext() {
		return postingList.hasLeft();
	}

	@Override
	public DocumentPosting next() {
		try {
			return postingList.next();
		} catch (IOException e) {
			LOGGER.severe("Error retrieving next document posting");
		}
		return null;
	}
}
