package de.hpi.ir.yahoogle.rm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Result implements Comparable<Result> {

	private final int docNumber;
	private final Map<String, Set<Integer>> positions = new HashMap<>();

	public Result(int docNumber) {
		this.docNumber = docNumber;
	}

	public void addPositions(String phrase, Set<Integer> positionList) {
		positions.put(phrase, positionList);
	}

	@Override
	public int compareTo(Result o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	public int getDocNumber() {
		return docNumber;
	}

	public Set<Integer> getPositions(String phrase) {
		return positions.getOrDefault(phrase, new HashSet<>());
	}

	public void merge(Result r2) {
		if (this.compareTo(r2) == 0) {
			r2.positions.entrySet().forEach(
					e -> positions.merge(e.getKey(), e.getValue(), (v1, v2) -> {
						v1.addAll(v2);
						return v1;
					}));
		}
	}
}
