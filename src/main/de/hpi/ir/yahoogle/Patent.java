package de.hpi.ir.yahoogle;

public class Patent {
	
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
		return this.getInventionTitle();
	}

}
