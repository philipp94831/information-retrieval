package de.hpi.ir.yahoogle.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparator<K, V extends Comparable<V>>
		implements Comparator<K> {

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueAscending(Map<K, V> result) {
		return new ValueComparator<>(result, true).sort(result);
	}

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		return new ValueComparator<>(result, false).sort(result);
	}
	
	private TreeMap<K, V> sort(Map<K, V> result) {
		TreeMap<K, V> sortedResults = new TreeMap<>(this);
		sortedResults.putAll(result);
		return sortedResults;		
	}

	private final boolean ascending;
	private final Map<K, V> base;

	private ValueComparator(Map<K, V> base, boolean ascending) {
		this.base = base;
		this.ascending = ascending;
	}

	@Override
	public int compare(K k1, K k2) {
		int c = base.get(k1).compareTo(base.get(k2));
		c = c == 0 ? 1 : c;
		return ascending ? c : -c;
	}
}
