package de.hpi.ir.yahoogle.util.merge;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MergeSortIterator<S extends Iterable<V>, V extends Mergeable<K>, K extends Comparable<K>>
		implements Iterator<List<V>> {

	private final TreeMap<K, List<MergeTreeEntry<V>>> candidates = new TreeMap<>();
	private final List<Iterator<V>> iterators;

	public MergeSortIterator(List<S> sources) {
		iterators = sources.stream().map(S::iterator)
				.collect(Collectors.toList());
		for (int i = 0; i < iterators.size(); i++) {
			Iterator<V> iterator = iterators.get(i);
			if (iterator.hasNext()) {
				put(iterator.next(), i);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !candidates.isEmpty();
	}

	@Override
	public List<V> next() {
		Entry<K, List<MergeTreeEntry<V>>> next = candidates.pollFirstEntry();
		List<V> values = new ArrayList<>();
		for (MergeTreeEntry<V> entry : next.getValue()) {
			values.add(entry.getValue());
			Iterator<V> iterator = iterators.get(entry.getIndex());
			if (iterator.hasNext()) {
				put(iterator.next(), entry.getIndex());
			}
		}
		return values;
	}

	private void put(V value, Integer i) {
		MergeTreeEntry<V> entry = new MergeTreeEntry<>(i, value);
		candidates.merge(value.getKey(), Collections.singletonList(entry),
				(v1, v2) -> {
					ArrayList<MergeTreeEntry<V>> list = new ArrayList<>();
					list.addAll(v1);
					list.addAll(v2);
					return list;
				});
	}
}
