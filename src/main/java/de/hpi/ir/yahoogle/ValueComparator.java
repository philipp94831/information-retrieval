package de.hpi.ir.yahoogle;

import java.util.Comparator;
import java.util.Map;

class ValueComparator<K, T extends Comparable<T>> implements Comparator<K> {

	private final Map<K, T> base;

	public ValueComparator(Map<K, T> base) {
		this.base = base;
	}

	@Override
	public int compare(K k1, K k2) {
		int c = base.get(k1).compareTo(base.get(k2));
		return c == 0 ? 1 : -c;
	}
}
