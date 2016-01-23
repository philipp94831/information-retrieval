package de.hpi.ir.yahoogle;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparator<K, T extends Comparable<T>> implements Comparator<K> {

	private final Map<K, T> base;
	private final boolean ascending;

	private ValueComparator(Map<K, T> base, boolean ascending) {
		this.base = base;
		this.ascending = ascending;
	}

	@Override
	public int compare(K k1, K k2) {
		int c = base.get(k1).compareTo(base.get(k2));
		c = c == 0 ? 1 : c;
		return ascending? c : -c;
	}

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<>(result, false);
		TreeMap<K, V> sortedResults = new TreeMap<>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueAscending(Map<K, V> result) {
		ValueComparator<K, V> comp = new ValueComparator<>(result, true);
		TreeMap<K, V> sortedResults = new TreeMap<>(comp);
		sortedResults.putAll(result);
		return sortedResults;
	}
}
