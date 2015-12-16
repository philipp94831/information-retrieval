package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import de.hpi.ir.yahoogle.index.PatentResume;

public class PatentResumeIterator implements Iterator<PatentResume> {

	private final static Logger LOGGER = Logger
			.getLogger(PatentResumeIterator.class.getName());
	private final PartialPatentIndex index;
	private long nextPosition;

	public PatentResumeIterator(PartialPatentIndex patentIndex) {
		this.index = patentIndex;
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
	public PatentResume next() {
		try {
			PatentResume resume = index.read(nextPosition);
			nextPosition = index.currentOffset();
			return resume;
		} catch (IOException e) {
			LOGGER.severe("Error retrieving next patent resume");
		}
		return null;
	}
}
