package de.hpi.ir.yahoogle.index.search;

import java.util.Iterator;
import java.util.List;

import de.hpi.ir.yahoogle.index.DocumentPosting;
import de.hpi.ir.yahoogle.util.MergeSortIterator;

public class TokenResultIterator implements Iterator<DocumentPosting> {

	private final MergeSortIterator<BinaryPostingListIterator, DocumentPosting, Integer> documents;

	public TokenResultIterator(List<BinaryPostingListIterator> sources) {
		documents = new MergeSortIterator<>(sources);
	}

	@Override
	public boolean hasNext() {
		return documents.hasNext();
	}

	@Override
	public DocumentPosting next() {
		List<DocumentPosting> list = documents.next();
		DocumentPosting result = null;
		for(DocumentPosting document : list) {
			if(result == null) {
				result = document;
				continue;
			}
			result.addAll(document);
		}
		return result;
	}
}
