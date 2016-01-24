package de.hpi.ir.yahoogle.rm;

public class QLResult extends Result {

	private double score;

	public QLResult(int docNumber) {
		super(docNumber);
	}

	public int compareScore(QLResult o2) {
		return -Double.compare(score, o2.score);
	}

	public void setScore(double score) {
		this.score = score;
	}
}
