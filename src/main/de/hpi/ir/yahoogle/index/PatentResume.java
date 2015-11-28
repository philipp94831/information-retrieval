package de.hpi.ir.yahoogle.index;

import java.io.Serializable;
import de.hpi.ir.yahoogle.Patent;

public class PatentResume implements Serializable {
	
	private static final long serialVersionUID = -61787145290197095L;
	
	private String inventionTitle;
	private int wordCount;
	private String patentAbstract;

	public PatentResume(Patent patent) {
		this.inventionTitle = patent.getInventionTitle();
		this.patentAbstract = patent.getPatentAbstract();
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

	public String getPatentAbstract() {
		return patentAbstract;
	}

	public void setPatentAbstract(String patentAbstract) {
		this.patentAbstract = patentAbstract;
	}

}
