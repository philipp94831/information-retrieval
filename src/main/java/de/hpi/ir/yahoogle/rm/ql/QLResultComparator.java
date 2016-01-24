package de.hpi.ir.yahoogle.rm.ql;

import java.util.Comparator;

public class QLResultComparator implements Comparator<QLResult> {

	@Override
	public int compare(QLResult o1, QLResult o2) {
		return o1.compareScore(o2);
	}
}
