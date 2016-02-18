package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import SearchEngine.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.parsing.Patent;
import de.hpi.ir.yahoogle.parsing.PatentParserCallback;

public class PatentReceiver implements PatentParserCallback {

	private static final Logger LOGGER = Logger
			.getLogger(PatentReceiver.class.getName());
	private static final long MAX_PATENTS = Runtime.getRuntime().maxMemory()
			/ (530 * 1000) / SearchEngineYahoogle.NUMBER_OF_THREADS;
	private PartialIndex currentIndex;
	private int indexNumber;
	private final String name;
	private final List<String> names = new ArrayList<>();
	private int patentsAdded;

	public PatentReceiver() {
		this("default");
	}

	public PatentReceiver(String name) {
		this.name = name;
	}

	private PartialIndex createPartialIndex() {
		String indexName = name + "." + indexNumber;
		PartialIndex index = new PartialIndex(indexName);
		names.add(indexName);
		indexNumber++;
		return index;
	}

	public void finish() throws IOException {
		currentIndex.write();
	}

	public List<String> getNames() {
		return names;
	}

	@Override
	public void receivePatent(Patent patent) {
		try {
			if (patentsAdded > MAX_PATENTS) {
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
		patentsAdded = 0;
	}
}
