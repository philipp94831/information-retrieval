package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class OffsetsIndex implements Serializable {
	
	private static final long serialVersionUID = -5696921632134023163L;
	private Map<String, Long> offsets = new HashMap<String, Long>();

	public void put(String key, long offset) {
		offsets.put(key, offset);
	}

	public Long get(String key) {
		return offsets.get(key);
	}

	public List<String> getTokensForPrefix(String prefix) {
		return offsets.keySet().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
	}

	public Set<Entry<String, Long>> entrySet() {
		return offsets.entrySet();
	}

	public Set<String> keys() {
		return offsets.keySet();
	}

}
