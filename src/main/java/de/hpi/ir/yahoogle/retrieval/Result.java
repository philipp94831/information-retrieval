package de.hpi.ir.yahoogle.retrieval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Result {

	private final int docNumber;
	protected final Map<String, Set<Integer>> positions = new HashMap<>();

	protected Result(int docNumber) {
		this.docNumber = docNumber;
	}

	public void addPositions(String phrase, Set<Integer> positionList) {
		positions.put(phrase, positionList);
	}

	public int getDocNumber() {
		return docNumber;
	}

	public Set<Integer> getPositions(String phrase) {
		return positions.getOrDefault(phrase, new HashSet<>());
	}
}
