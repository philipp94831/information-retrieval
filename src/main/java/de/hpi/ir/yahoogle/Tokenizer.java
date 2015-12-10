package de.hpi.ir.yahoogle;

import java.util.Iterator;
import java.util.StringTokenizer;

public class Tokenizer implements Iterator<String> {

	private String nextToken;
	private int pos = 0;
	private String separator = " ";
	private boolean skipStopWords = false;
	private final StringTokenizer string;

	public Tokenizer(String string) {
		this.string = new StringTokenizer(string);
	}

	public Tokenizer(String string, boolean skipStopWords) {
		this(string);
		this.skipStopWords = skipStopWords;
	}

	public int getPosition() {
		return pos;
	}

	@Override
	public boolean hasNext() {
		if (skipStopWords) {
			if (string.hasMoreTokens()) {
				nextToken = string.nextToken();
				while (StopWordList.isStopword(nextToken)) {
					if (string.hasMoreTokens()) {
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
		String token;
		if (skipStopWords) {
			token = nextToken;
		} else {
			token = string.nextToken();
		}
		if (!StopWordList.isStopword(token)) {
			pos++;
		}
		return token;
	}

	public String separator() {
		return separator;
	}
}
