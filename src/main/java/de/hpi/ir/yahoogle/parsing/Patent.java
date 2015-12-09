package de.hpi.ir.yahoogle.parsing;

public class Patent {

	private int docNumber;
	private long end;
	private String fileName;
	private String inventionTitle;
	private String patentAbstract;
	private long start;

	public Patent(String fileName) {
		this.fileName = fileName;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public long getEnd() {
		return end;
	}

	public String getFileName() {
		return fileName;
	}

	public String getInventionTitle() {
		return inventionTitle;
	}

	public String getPatentAbstract() {
		return patentAbstract;
	}

	public long getStart() {
		return start;
	}

	public void setDocNumber(int docNumber) {
		this.docNumber = docNumber;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setInventionTitle(String inventionTitle) {
		this.inventionTitle = inventionTitle;
	}

	public void setPatentAbstract(String patentAbstract) {
		this.patentAbstract = patentAbstract;
	}

	public void setStart(long start) {
		this.start = start;
	}
}
