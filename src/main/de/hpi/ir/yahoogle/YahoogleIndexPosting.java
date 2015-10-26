package de.hpi.ir.yahoogle;

public class YahoogleIndexPosting {

	private int docNumber;
	private short position;
	private String token;

	public YahoogleIndexPosting(String token) {
		this.token = token;
	}

	public int getDocNumber() {
		return docNumber;
	}

	public short getPosition() {
		return position;
	}

	public String getToken() {
		return token;
	}

	public void setDocNumber(int docNumber) {
		this.docNumber = docNumber;
	}

	public void setPosition(short position) {
		this.position = position;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
