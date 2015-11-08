package de.hpi.ir.yahoogle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopWordList {

	private static final String STOPWORDS_FILE = SearchEngineYahoogle.teamDirectory + "/stopwords.txt";
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

}
