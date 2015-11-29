package de.hpi.ir.yahoogle;

import org.tartarus.snowball.ext.englishStemmer;

public class Stemmer {
	
	public static String stem(String word) {
		englishStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(word.toLowerCase().replaceAll("\\W", ""));
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
