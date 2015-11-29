package de.hpi.ir.yahoogle.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.io.ByteReader;

public class PatentResume implements Serializable {
	
	private static final long serialVersionUID = -61787145290197095L;
	
	private int wordCount;
	private String fileName;
	private long titleOffset;
	private long abstractOffset;
	
	private String inventionTitle;
	private String patentAbstract;
	
	public PatentResume(Patent patent) {
		this.fileName = patent.getFileName();
		this.titleOffset = patent.getTitleOffset();
		this.abstractOffset = patent.getAbstractOffset();
		this.inventionTitle = patent.getInventionTitle();
		this.patentAbstract = patent.getPatentAbstract();
	}

	public String getInventionTitle() {
		return inventionTitle;
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

}
