package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import de.hpi.ir.yahoogle.index.BinaryCitationList;

public class CitationListIterator implements Iterator<BinaryCitationList> {

	private static final Logger LOGGER = Logger
			.getLogger(CitationListIterator.class.getName());
	private final PartialCitationIndex index;
	private long nextPosition;

	public CitationListIterator(PartialCitationIndex partialCitationIndex) {
		this.index = partialCitationIndex;
	}

	@Override
	public boolean hasNext() {
		try {
			return nextPosition < index.fileSize();
		} catch (IOException e) {
			LOGGER.severe("Error checking file size of partial index");
		}
		return false;
	}

	@Override
	public BinaryCitationList next() {
		try {
			BinaryCitationList citationList = index.read(nextPosition);
			nextPosition = index.currentOffset();
			return citationList;
		} catch (IOException e) {
			LOGGER.severe("Error retrieving next citation list");
		}
		return null;
	}
}
