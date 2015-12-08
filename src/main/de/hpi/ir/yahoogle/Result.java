package de.hpi.ir.yahoogle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Result {

	private int docNumber;
	private Map<String, Set<Integer>> positions = new HashMap<String, Set<Integer>>();

	public Result(int docNumber) {
		this.docNumber = docNumber;
	}

	public void addPositions(String phrase, Set<Integer> positionList) {
		positions.put(phrase, positionList);
	}

	public int getDocNumber() {
		return docNumber;
	}

	public Set<Integer> getPositions(String phrase) {
		return positions.get(phrase);
	}

}
