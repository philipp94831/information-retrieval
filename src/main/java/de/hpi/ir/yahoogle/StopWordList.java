package de.hpi.ir.yahoogle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class StopWordList {

	private static final Set<String> stopwords = new HashSet<>();
	private static final String STOPWORDS_FILE = SearchEngineYahoogle
			.getTeamDirectory() + "/stopwords.txt";

	static {
		FileReader fr;
		try {
			fr = new FileReader(STOPWORDS_FILE);
			BufferedReader br = new BufferedReader(fr);
			String stopword;
			while ((stopword = br.readLine()) != null) {
				stopwords.add(Stemmer.stem(stopword));
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isStopword(String word) {
		return !word.endsWith("*") && stopwords.contains(Stemmer.stem(word));
	}
}
