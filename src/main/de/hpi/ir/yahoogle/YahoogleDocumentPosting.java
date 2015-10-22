package de.hpi.ir.yahoogle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class YahoogleDocumentPosting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034659269859348131L;
	
	List<YahoogleIndexPosting> postings = new ArrayList<YahoogleIndexPosting>();
	
	public void add(YahoogleIndexPosting posting) {
		postings.add(posting);
	}

}
