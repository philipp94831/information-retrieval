package de.hpi.ir.yahoogle.rm;

import java.util.List;
import java.util.StringTokenizer;

import de.hpi.ir.yahoogle.index.Index;

public abstract class Model {

	protected static int wordCount(String phrase) {
		StringTokenizer tokenizer = new StringTokenizer(phrase);
		return tokenizer.countTokens();
	}

	protected static int wordCount(String phrase, String word) {
		StringTokenizer tokenizer = new StringTokenizer(phrase);
		int wc = 0;
		while (tokenizer.hasMoreTokens()) {
			if (tokenizer.nextToken().equals(word)) {
				wc++;
			}
		}
		return wc;
	}

	protected final Index index;

	public Model(Index index) {
		this.index = index;
	}

	public abstract List<ModelResult> compute(List<String> query);
}
