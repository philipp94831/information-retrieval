package de.hpi.ir.yahoogle.rm.bool;

import de.hpi.ir.yahoogle.rm.Result;

public class BooleanResult extends Result implements Comparable<BooleanResult> {

	public BooleanResult(int docNumber) {
		super(docNumber);
	}
	
	@Override
	public int compareTo(BooleanResult o2) {
		return -Integer.compare(getDocNumber(), o2.getDocNumber());
	}
}
