package de.hpi.ir.yahoogle;

public class Patent {

	private int docNumber;
	private String inventionTitle;
	private String patentAbstract;

	public int getDocNumber() {
		return docNumber;
	}

	public String getInventionTitle() {
		return inventionTitle;
	}

	public String getPatentAbstract() {
		return patentAbstract;
	}

	public void setDocNumber(int docNumber) {
		this.docNumber = docNumber;
	}

	public void setInventionTitle(String inventionTitle) {
		this.inventionTitle = inventionTitle;
	}

	public void setPatentAbstract(String patentAbstract) {
		this.patentAbstract = patentAbstract;
	}

}
