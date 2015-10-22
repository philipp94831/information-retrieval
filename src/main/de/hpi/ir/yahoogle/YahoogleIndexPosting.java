package de.hpi.ir.yahoogle;

import java.io.Serializable;

public class YahoogleIndexPosting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 834610298197650575L;
	
	private int position;
	private String originalToken;

	public String getOriginalToken() {
		return originalToken;
	}

	public void setOriginalToken(String originalToken) {
		this.originalToken = originalToken;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
