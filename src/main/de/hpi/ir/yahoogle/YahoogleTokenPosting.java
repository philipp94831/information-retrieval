package de.hpi.ir.yahoogle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YahoogleTokenPosting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8768915611186115196L;
	
	private Map<String, YahoogleDocumentPosting> documents = new HashMap<String, YahoogleDocumentPosting>();

	public void add(String docNumber, YahoogleIndexPosting posting) {
		if(documents.get(docNumber) == null) {
			documents.put(docNumber, new YahoogleDocumentPosting());
		}
		documents.get(docNumber).add(posting);
	}

	public Set<String> getDocNumbers() {
		return documents.keySet();
	}
	
}
