package de.hpi.ir.yahoogle.rm;

public class ModelResult extends Result implements Comparable<ModelResult> {

	private double score;

	public ModelResult(int docNumber) {
		super(docNumber);
	}

	@Override
	public int compareTo(ModelResult o) {
		return -Double.compare(score, o.score);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
