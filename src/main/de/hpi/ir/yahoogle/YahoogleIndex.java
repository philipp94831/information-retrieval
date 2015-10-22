package de.hpi.ir.yahoogle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7975583843980333170L;
	
	private Map<String, YahoogleTokenPosting> tokens = new HashMap<String, YahoogleTokenPosting>();
	
	public void add(String token, String docNumber, YahoogleIndexPosting posting) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		token = stemmer.stem(token);
		token = token.replaceAll("\\W", "");
		if(tokens.get(token) == null) {
			tokens.put(token, new YahoogleTokenPosting());
		}
		tokens.get(token).add(docNumber, posting);
	}
	
	public Set<String> getDocNumbers(String token) {
		return tokens.get(token).getDocNumbers();
	}
	
}
