package de.hpi.ir.yahoogle.index;

import java.io.Serializable;

import de.hpi.ir.yahoogle.Patent;

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
