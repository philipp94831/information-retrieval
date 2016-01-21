package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.hpi.ir.yahoogle.parsing.Patent;
import de.hpi.ir.yahoogle.parsing.PatentParserCallback;

public class PartialIndexFactory implements PatentParserCallback {

	private static final Logger LOGGER = Logger
			.getLogger(PartialIndexFactory.class.getName());
	private int indexNumber;
	private final List<String> names = new ArrayList<>();
	private static final int MAX_PATENTS = 10000;
	private int patentsAdded;
	private PartialIndex currentIndex;
	private final String name;	
	
	public PartialIndexFactory() {
		this("default");
	}

	public PartialIndexFactory(String name) {
		this.name = name;
	}

	public List<String> getNames() {
		return names;
	}

	public PartialIndex createPartialIndex() {
		String indexName = name + "." + indexNumber;
		PartialIndex index = new PartialIndex(indexName);
		names.add(indexName);
		indexNumber++;
		return index;
	}

	@Override
	public void receivePatent(Patent patent) {
		try {
			if(patentsAdded > MAX_PATENTS) {
				finish();
				start();
			}
			currentIndex.add(patent);
			patentsAdded++;
		} catch (IOException e) {
			LOGGER.severe("Error creating or writing partial index");
		}
	}

	public void start() throws IOException {
		currentIndex = createPartialIndex();
		currentIndex.create();
	}

	public void finish() throws IOException {
		currentIndex.write();
	}
}
