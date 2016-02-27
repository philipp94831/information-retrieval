package de.hpi.ir.yahoogle.util.merge;

public interface Mergeable<T extends Comparable<T>> {

	T getKey();
}
