package de.hpi.ir.yahoogle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class StopWordList {

	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static Set<String> stopwords = new HashSet<String>();;

	static {
		FileReader fr;
		try {
			fr = new FileReader(STOPWORDS_FILE);
			BufferedReader br = new BufferedReader(fr);
			String stopword;
			while ((stopword = br.readLine()) != null) {
				stopwords.add(YahoogleUtils.sanitize(stopword));
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isStopword(String word) {
		return stopwords.contains(YahoogleUtils.sanitize(word));
	}

	public static boolean allStopwords(String phrase) {
		boolean result = true;
		StringTokenizer tokenizer = new StringTokenizer(phrase);
		while(tokenizer.hasMoreTokens()) {
			result &= isStopword(tokenizer.nextToken());
		}
		return result;
	}

}
