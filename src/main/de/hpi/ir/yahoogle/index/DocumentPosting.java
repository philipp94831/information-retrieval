package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class DocumentPosting implements Serializable, Comparable<DocumentPosting>, Iterable<Posting> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4418725649156340031L;
	private int docNumber;
	private Set<Posting> postings = new TreeSet<Posting>();

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
