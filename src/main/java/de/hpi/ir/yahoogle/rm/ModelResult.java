package de.hpi.ir.yahoogle.rm;

public class ModelResult extends Result {

	private double score;

	public ModelResult(int docNumber) {
		super(docNumber);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
