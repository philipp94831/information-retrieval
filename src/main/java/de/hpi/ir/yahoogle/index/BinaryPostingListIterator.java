package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

public class BinaryPostingListIterator implements Iterable<DocumentPosting>, Iterator<DocumentPosting> {

	private static final Logger LOGGER = Logger.getLogger(BinaryPostingListIterator.class.getName());
	private final int count;
	private long currentPosition;
	private final TokenDictionary tokenDictionary;
	private int partsRead;
	private Iterator<DocumentPosting> currentIterator;

	public BinaryPostingListIterator(TokenDictionary tokenDictionary, int count, long currentPosition)
			throws IOException {
		this.tokenDictionary = tokenDictionary;
		this.count = count;
		this.currentPosition = currentPosition;
		getNextPart();
	}

	private void getNextPart() throws IOException {
		BinaryPostingList binaryPostingList = tokenDictionary.getBinaryPostingList(currentPosition);
		currentIterator = binaryPostingList.iterator();
		currentPosition += binaryPostingList.getBytes().length + Integer.BYTES;
		partsRead++;
	}

	@Override
	public boolean hasNext() {
		if (currentIterator.hasNext()) {
			return true;
		} else {
			if (partsRead < count) {
				try {
					getNextPart();
					return currentIterator.hasNext();
				} catch (IOException e) {
					LOGGER.severe("Error reading next BinaryPostingList");
					;
				}
			}
		}
		return false;
	}

	@Override
	public DocumentPosting next() {
		return currentIterator.next();
	}

	@Override
	public Iterator<DocumentPosting> iterator() {
		return this;
	}
}
