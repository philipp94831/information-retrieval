package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.ir.yahoogle.index.PatentResume;

public class PatentResumeIterator implements Iterator<PatentResume> {

	private PartialPatentIndex index;
	private long nextPosition;

	public PatentResumeIterator(PartialPatentIndex patentIndex) {
		this.index = patentIndex;
	}

	@Override
	public boolean hasNext() {
		try {
			return nextPosition < index.fileSize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
