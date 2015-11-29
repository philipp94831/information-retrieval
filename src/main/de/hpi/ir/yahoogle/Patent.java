package de.hpi.ir.yahoogle;

public class Patent {

	private int docNumber;
	private String inventionTitle;
	private String patentAbstract;
	private String fileName;
	private long titleOffset;
	private long abstractOffset;

	public Patent(String fileName) {
		this.fileName = fileName;
	}

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

	public String getFileName() {
		return fileName;
	}

	public long getTitleOffset() {
		return titleOffset;
	}

	public void setTitleOffset(long titleOffset) {
		this.titleOffset = titleOffset;
	}

	public long getAbstractOffset() {
		return abstractOffset;
	}

	public void setAbstractOffset(long abstractOffset) {
		this.abstractOffset = abstractOffset;
	}

}
