package de.hpi.ir.yahoogle;

import java.io.Serializable;

public class PatentResume implements Serializable {
	
	private static final long serialVersionUID = -61787145290197095L;
	
	private String inventionTitle;

	public PatentResume(Patent patent) {
		this.inventionTitle = patent.getInventionTitle();
	}

	public String getInventionTitle() {
		return inventionTitle;
	}

	public void setInventionTitle(String inventionTitle) {
		this.inventionTitle = inventionTitle;
	}

}
