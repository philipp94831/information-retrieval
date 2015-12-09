package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.ir.yahoogle.index.BinaryPostingList;

public class PostingListIterator implements Iterator<BinaryPostingList> {

	private PartialTokenDictionary index;
	private long nextPosition;

	public PostingListIterator(PartialTokenDictionary tokenDictionary) {
		this.index = tokenDictionary;
	}

	@Override
	public boolean hasNext() {
		try {
			return nextPosition < index.fileSize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
