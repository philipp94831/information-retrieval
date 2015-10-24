package de.hpi.ir.yahoogle;

/**
 *
 * @author: Yahoogle
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * This is your file! implement your search engine here!
 * 
 * Describe your search engine briefly:
 *  - multi-threaded?
 *  - stemming?
 *  - stopword removal?
 *  - index algorithm?
 *  - etc.  
 * 
 * Keep in mind to include your implementation decisions also in the pdf file of each assignment
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.lemurproject.kstem.KrovetzStemmer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template'
															// with your search
															// engine's name,
															// i.e.
															// SearchEngineMyTeamName

	private YahoogleIndex index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
    void index(String directory) {
		String fileName = "res/testData.xml";
    	
    	try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
	    	
			PatentIndexer handler = new PatentIndexer();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			
			FileReader r = new FileReader(fileName);
			xr.parse(new InputSource(r));
			
			index = handler.getIndex();
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	@Override
	boolean loadIndex(String directory) {
		index = new YahoogleIndex();
		return index.load();
	}

	@Override
	void compressIndex(String directory) {
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		return false;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		Set<String> docNumbers = null;
		KrovetzStemmer stemmer = new KrovetzStemmer();
		if(tokenizer.hasMoreTokens()) {
			docNumbers = index.find(stemmer.stem(tokenizer.nextToken()));
		}
		while(tokenizer.hasMoreTokens()) {
			docNumbers.retainAll(index.find(stemmer.stem(tokenizer.nextToken())));
		}
		return index.match(docNumbers);
	}

}
