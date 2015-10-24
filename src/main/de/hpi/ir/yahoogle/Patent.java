package de.hpi.ir.yahoogle;

import java.io.Serializable;

public class Patent implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5928796102621977656L;
	private String patentAbstract;
	private String docNumber;
	private String inventionTitle;
	
	public String getPatentAbstract() {
		return patentAbstract;
	}
	public void setPatentAbstract(String patentAbstract) {
		this.patentAbstract = patentAbstract;
	}
	public String getDocNumber() {
		return docNumber;
	}
	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}
	public void setInventionTitle(String inventionTitle) {
		this.inventionTitle = inventionTitle;
	}
	public String getInventionTitle() {
		return inventionTitle;
	}
	
	@Override
	public String toString() {
		return this.getInventionTitle() + ": " + this.getPatentAbstract();
	}

}
