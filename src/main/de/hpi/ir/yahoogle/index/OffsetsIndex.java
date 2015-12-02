package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class OffsetsIndex<K> implements Serializable {
	
	private static final long serialVersionUID = -5696921632134023163L;
	private Map<K, Long> offsets = new HashMap<K, Long>();

	public void put(K key, long offset) {
		offsets.put(key, offset);
	}

	public Long get(K key) {
		return offsets.get(key);
	}

	public Set<Entry<K, Long>> entrySet() {
		return offsets.entrySet();
	}

	public Set<K> keys() {
		return offsets.keySet();
	}

}
