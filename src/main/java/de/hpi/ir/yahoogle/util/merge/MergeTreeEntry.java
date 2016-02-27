package de.hpi.ir.yahoogle.util.merge;

class MergeTreeEntry<V> {

	private final Integer index;
	private final V value;

	public MergeTreeEntry(Integer i, V value) {
		this.index = i;
		this.value = value;
	}

	public Integer getIndex() {
		return index;
	}

	public V getValue() {
		return value;
	}
}
