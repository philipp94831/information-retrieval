package de.hpi.ir.yahoogle;

public class YahoogleIndexPosting implements Comparable<YahoogleIndexPosting> {

	private String docNumber;
	private int position;
	private String token;

	public YahoogleIndexPosting(String token) {
		this.token = token;
	}

	public String getDocNumber() {
		return docNumber;
	}

	public int getPosition() {
		return position;
	}

	public String getToken() {
		return token;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public int compareTo(YahoogleIndexPosting post) {
		return token.compareTo(post.getToken());
	}

}
