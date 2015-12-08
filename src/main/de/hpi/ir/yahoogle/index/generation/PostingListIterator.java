package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.ir.yahoogle.index.BinaryPostingList;

public class PostingListIterator implements Iterator<BinaryPostingList> {

	private long nextPosition;
	private TokenDictionary index;
	
	public PostingListIterator(TokenDictionary tokenDictionary) {
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
