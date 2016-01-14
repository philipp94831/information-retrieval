package de.hpi.ir.yahoogle;

import java.util.Iterator;
import java.util.StringTokenizer;

public class Tokenizer implements Iterator<String> {

	private static final String DELIM = "- \t\n\r\f";
	private String nextToken;
	private int pos = 0;
	private final boolean returnDelims;
	private final boolean skipStopWords;
	private final String string;
	private StringTokenizer tokenizer;
	private boolean realToken;

	public Tokenizer(String string) {
		this(string, true, false);
	}

	public Tokenizer(String string, boolean skipStopWords) {
		this(string, skipStopWords, false);
	}

	public Tokenizer(String string, boolean skipStopWords, boolean returnDelims) {
		this.string = string;
		this.returnDelims = returnDelims;
		this.tokenizer = new StringTokenizer(string, DELIM, returnDelims);
		this.skipStopWords = skipStopWords;
	}

	public int countTokens() {
		StringTokenizer old = tokenizer;
		int oldPos = pos;
		pos = 0;
		tokenizer = new StringTokenizer(string, DELIM, returnDelims);
		while (hasNext()) {
			next();
		}
		int result = pos;
		tokenizer = old;
		pos = oldPos;
		return result;
	}

	public int getPosition() {
		return pos;
	}

	@Override
	public boolean hasNext() {
		if (skipStopWords) {
			if (tokenizer.hasMoreTokens()) {
				nextToken = tokenizer.nextToken();
				while (StopWordList.isStopword(nextToken)) {
					if (tokenizer.hasMoreTokens()) {
						nextToken = tokenizer.nextToken();
					} else {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return tokenizer.hasMoreTokens();
		}
	}

	@Override
	public String next() {
		String token;
		realToken = false;
		if (skipStopWords) {
			token = nextToken;
		} else {
			token = tokenizer.nextToken();
		}
		if (!StopWordList.isStopword(token)
				&& !token.matches("[" + DELIM + "]+")) {
			realToken = true;
			pos++;
		}
		return token;
	}
	
	public boolean isRealToken() {
		return realToken;
	}
}
