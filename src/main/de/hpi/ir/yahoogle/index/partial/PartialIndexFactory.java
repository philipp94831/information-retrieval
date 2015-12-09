package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartialIndexFactory {

	private int indexNumber;
	private List<String> names = new ArrayList<String>();

	public List<String> getNames() {
		return names;
	}

	public PartialIndex getPartialIndex() throws IOException {
		String indexName = Integer.toString(indexNumber);
		PartialIndex index = new PartialIndex(indexName);
		names.add(indexName);
		indexNumber++;
		return index;
	}
}
