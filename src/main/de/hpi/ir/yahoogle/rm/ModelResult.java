package de.hpi.ir.yahoogle.rm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModelResult implements Comparable<ModelResult> {
	
	private double score;
	private int docNumber;
	private Map<String, Set<Integer>> positions = new HashMap<String, Set<Integer>>();
	
	public ModelResult(int docNumber) {
		this.docNumber = docNumber;
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

	public void addPositions(String phrase, Set<Integer> positionList)	{
		positions.put(phrase, positionList);
	}
	
	public Set<Integer> getPositions(String phrase) {
		return positions.get(phrase);
	}

	public int getDocNumber() {
		return docNumber;
	}

}
