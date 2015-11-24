package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import de.hpi.ir.yahoogle.Patent;

public class PatentResume implements Serializable {
	
	private static final long serialVersionUID = -61787145290197095L;
	
	private String inventionTitle;
	private int wordCount;
	private Map<String, Double> wordFrequencies = new HashMap<String, Double>();

	public PatentResume(Patent patent) {
		this.inventionTitle = patent.getInventionTitle();
	}

	public String getInventionTitle() {
		return inventionTitle;
	}

	public void setInventionTitle(String inventionTitle) {
		this.inventionTitle = inventionTitle;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public Map<String, Double> getWordFrequencies() {
		return wordFrequencies;
	}

	public void setWordFrequencies(HashMap<String, Double> strippedMap) {
		this.wordFrequencies = strippedMap;
	}

}
