package de.hpi.ir.yahoogle.language;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import SearchEngine.SearchEngineYahoogle;

public class StopWordList {

	private static final Logger LOGGER = Logger
			.getLogger(StopWordList.class.getName());
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
			LOGGER.severe("Error reading stopword list");
		}
	}

	public static boolean isStopword(String word) {
		String stem = Stemmer.stem(word);
		return !word.endsWith("*")
				&& (word.length() < 2 || stopwords.contains(stem)
						|| stem.matches("[^a-zA-Z]*") || stem.trim().isEmpty());
	}
}
