package de.hpi.ir.yahoogle;

import java.io.File;
import org.tartarus.snowball.ext.englishStemmer;

public class YahoogleUtils {

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

	public static String sanitize(String word) {
		englishStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(word.toLowerCase().replaceAll("\\W", ""));
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
