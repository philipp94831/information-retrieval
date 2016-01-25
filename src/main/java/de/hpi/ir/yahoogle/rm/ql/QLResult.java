package de.hpi.ir.yahoogle.rm.ql;

import de.hpi.ir.yahoogle.rm.Result;

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
		return -Double.compare(score, o2.score);
	}
}
