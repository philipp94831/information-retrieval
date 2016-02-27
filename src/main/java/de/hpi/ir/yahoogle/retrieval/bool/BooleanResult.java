package de.hpi.ir.yahoogle.retrieval.bool;

import de.hpi.ir.yahoogle.retrieval.Result;

public class BooleanResult extends Result implements Comparable<BooleanResult> {

	public BooleanResult(int docNumber) {
		super(docNumber);
	}
	
	@Override
	public int compareTo(BooleanResult o2) {
		return -Integer.compare(getDocNumber(), o2.getDocNumber());
	}

	public BooleanResult merge(BooleanResult r2) {
		r2.positions.entrySet().forEach(
				e -> positions.merge(e.getKey(), e.getValue(), (v1, v2) -> {
					v1.addAll(v2);
					return v1;
				}));
		return this;
	}
}
