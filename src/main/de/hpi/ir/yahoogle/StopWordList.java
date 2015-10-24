package de.hpi.ir.yahoogle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopWordList {

	private Set<String> stopwords = new HashSet<String>();;

	public StopWordList(String fileName) {
		FileReader fr;
		try {
			fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String stopword;
			while ((stopword = br.readLine()) != null) {
				stopwords.add(stopword);
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
	
	public boolean contains(String word) {
		return stopwords.contains(word);
	}

}
