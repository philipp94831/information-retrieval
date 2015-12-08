package de.hpi.ir.yahoogle;

import java.util.Iterator;
import java.util.StringTokenizer;

public class Tokenizer implements Iterator<String> {
	
	private StringTokenizer string;
	private int pos = 0;
	private boolean skipStopWords = false;
	private String nextToken;
	
	public Tokenizer(String string, boolean skipStopWords) {
		this(string);
		this.skipStopWords = skipStopWords;
	}
	
	public Tokenizer(String string) {
		this.string = new StringTokenizer(string);
	}

	@Override
	public boolean hasNext() {
		if(skipStopWords) {
			if(string.hasMoreTokens()) {
				nextToken = string.nextToken();
				while(StopWordList.isStopword(nextToken)) {
					if(string.hasMoreTokens()) {
						nextToken = string.nextToken();					
					} else {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return string.hasMoreTokens();
		}
	}

	@Override
	public String next() {
		pos++;
		if(skipStopWords) {
			return nextToken;
		}
		else {
			return string.nextToken();
		}
	}
	
	public int getPosition() {
		return pos;
	}

}
