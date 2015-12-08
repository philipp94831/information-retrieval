package de.hpi.ir.yahoogle.index.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class SkippableOffsetsIndex<K> extends Loadable {

	private static final String BASE_NAME = ".offsets";
	private String name;
	private Map<K, Long> offsets;

	public SkippableOffsetsIndex(String name) {
		this.name = name;
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		offsets = new HashMap<K, Long>();
	}

	public Set<Entry<K, Long>> entrySet() {
		return offsets.entrySet();
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	public Long get(K key) {
		return offsets.get(key);
	}

	public Set<K> keys() {
		return offsets.keySet();
	}

	@Override
	public void load() throws IOException {
		offsets = ObjectReader.<HashMap<K, Long>> readObject(fileName());
	}

	public void put(K key, long offset) {
		offsets.put(key, offset);
	}

	@Override
	public void write() throws IOException {
		ObjectWriter.writeObject(offsets, fileName());
	}

}
