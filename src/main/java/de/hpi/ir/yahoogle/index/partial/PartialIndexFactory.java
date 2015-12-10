package de.hpi.ir.yahoogle.index.partial;

import java.util.ArrayList;
import java.util.List;

public class PartialIndexFactory {

	private int indexNumber;
	private final List<String> names = new ArrayList<>();

	public List<String> getNames() {
		return names;
	}

	public PartialIndex getPartialIndex() {
		String indexName = Integer.toString(indexNumber);
		PartialIndex index = new PartialIndex(indexName);
		names.add(indexName);
		indexNumber++;
		return index;
	}
}
