package de.hpi.ir.yahoogle.retrieval.ql;

import de.hpi.ir.yahoogle.retrieval.Result;

public class QLResult extends Result implements Comparable<QLResult> {

	private double score;

	public QLResult(int docNumber) {
		super(docNumber);
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public int compareTo(QLResult o2) {
		int comp = -Double.compare(score, o2.score);
		return comp == 0? -Integer.compare(getDocNumber(), o2.getDocNumber()) : comp;
	}
}
