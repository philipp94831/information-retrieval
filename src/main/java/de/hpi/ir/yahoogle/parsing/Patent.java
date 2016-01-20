package de.hpi.ir.yahoogle.parsing;

import java.util.ArrayList;
import java.util.List;

public class Patent {

	private final List<String> claims = new ArrayList<>();
	private final List<String> descriptions = new ArrayList<>();
	private int docNumber;
	private long end;
	private final String fileName;
	private String inventionTitle;
	private String patentAbstract;
	private long start;

	public Patent(String fileName) {
		this.fileName = fileName;
	}

	public void addClaim(String string) {
		claims.add(string);
	}

	public void addDescription(String string) {
		descriptions.add(string);
	}

	public List<String> getClaims() {
		return claims;
	}

	public List<String> getDescriptions() {
		return descriptions;
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

	public List<Integer> getCitations() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}
}
