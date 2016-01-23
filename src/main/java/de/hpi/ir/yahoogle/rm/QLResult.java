package de.hpi.ir.yahoogle.rm;


public class QLResult extends Result {

	public QLResult(int docNumber) {
		super(docNumber);
	}

	@Override
	public int compareScore(Result o2) {
		return -Double.compare(score, o2.score);
	}
}
