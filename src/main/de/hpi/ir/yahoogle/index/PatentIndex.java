package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PatentIndex implements Serializable {
	
	private static final long serialVersionUID = 5843638652062695334L;
	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private int totalWordCount = 0;
	
	public void add(int docNumber, PatentResume resume) {
		totalWordCount += resume.getWordCount();
		patents.put(docNumber, resume);
	}

	public Set<Integer> getAllDocNumbers() {
		return new HashSet<Integer>(patents.keySet());
	}

	public int wordCount(Integer docNumber) {
		return patents.get(docNumber).getWordCount();
	}

	public int getTotalWordCount() {
		return totalWordCount;
	}

	public PatentResume get(Integer docNumber) {
		return patents.get(docNumber);
	}

}
