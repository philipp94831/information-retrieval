package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import de.hpi.ir.yahoogle.index.BinaryPostingList;

public class PostingListIterator implements Iterator<BinaryPostingList> {

	private static final Logger LOGGER = Logger
			.getLogger(PostingListIterator.class.getName());
	private final PartialTokenDictionary index;
	private long nextPosition;

	public PostingListIterator(PartialTokenDictionary tokenDictionary) {
		this.index = tokenDictionary;
	}

	@Override
	public boolean hasNext() {
		try {
			return nextPosition < index.fileSize();
		} catch (IOException e) {
			LOGGER.severe("Error checking file size of partial index");
		}
		return false;
	}

	@Override
	public BinaryPostingList next() {
		try {
			BinaryPostingList postingList = index.read(nextPosition);
			nextPosition = index.currentOffset();
			return postingList;
		} catch (IOException e) {
			LOGGER.severe("Error retrieving next posting list");
		}
		return null;
	}
}
