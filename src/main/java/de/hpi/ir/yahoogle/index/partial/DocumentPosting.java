package de.hpi.ir.yahoogle.index.partial;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class DocumentPosting implements Comparable<DocumentPosting>, Iterable<Posting> {

	private final int docNumber;
	private final Set<Posting> postings = new TreeSet<>();

	public DocumentPosting(int docNumber) {
		this.docNumber = docNumber;
	}

	public void add(Posting posting) {
		postings.add(posting);
	}

	@Override
	public int compareTo(DocumentPosting o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	public Set<Posting> getAll() {
		return postings;
	}

	public int getDocNumber() {
		return docNumber;
	}

	@Override
	public Iterator<Posting> iterator() {
		return postings.iterator();
	}

	public int size() {
		return postings.size();
	}
}
