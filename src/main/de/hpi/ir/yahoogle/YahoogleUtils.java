package de.hpi.ir.yahoogle;

import java.io.File;
import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleUtils {

	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	public static String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

}
