package de.hpi.ir.yahoogle.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparator<K, V extends Comparable<V>>
		implements Comparator<K> {

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueAscending(Map<K, V> result) {
		return new ValueComparator<>(result, true).sort();
	}

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		return new ValueComparator<>(result, false).sort();
	}

	private final boolean ascending;
	private final Map<K, V> base;
	private final TreeMap<K, V> sortedResults;

	private ValueComparator(Map<K, V> base, boolean ascending) {
		this.base = base;
		this.ascending = ascending;
		sortedResults = new TreeMap<>(this);
		sortedResults.putAll(base);
	}

	@Override
	public int compare(K k1, K k2) {
		int c = base.get(k1).compareTo(base.get(k2));
		c = c == 0 ? 1 : c;
		return ascending ? c : -c;
	}

	private TreeMap<K, V> sort() {
		return sortedResults;
	}
}
