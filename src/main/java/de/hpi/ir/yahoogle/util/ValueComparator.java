package de.hpi.ir.yahoogle.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueAscending(Map<K, V> result) {
		return new ValueComparator<>(result, true).sort();
	}

	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValueDescending(Map<K, V> result) {
		return new ValueComparator<>(result, false).sort();
	}

	private final boolean ascending;
	private final Map<K, V> base;
	private final int maxSize;
	private final TreeMap<K, V> sortedResults;

	public ValueComparator(boolean ascending) {
		this(Integer.MAX_VALUE, ascending);
	}

	public ValueComparator(int maxSize, boolean ascending) {
		this(new HashMap<>(), ascending, maxSize);
	}

	public ValueComparator(Map<K, V> base, boolean ascending) {
		this(base, ascending, Integer.MAX_VALUE);
	}

	private ValueComparator(Map<K, V> base, boolean ascending, int maxSize) {
		this.base = base;
		this.ascending = ascending;
		this.maxSize = maxSize;
		sortedResults = new TreeMap<>(this);
		sortedResults.putAll(base);
	}

	@Override
	public int compare(K k1, K k2) {
		int c = base.get(k1).compareTo(base.get(k2));
		c = c == 0 ? 1 : c;
		return ascending ? c : -c;
	}

	public void put(K key, V value) {
		while (base.size() + 1 >= maxSize) {
			Entry<K, V> entry = sortedResults.pollLastEntry();
			base.remove(entry.getKey());
		}
		base.put(key, value);
		sortedResults.put(key, value);
	}

	private TreeMap<K, V> sort() {
		return sortedResults;
	}
}
