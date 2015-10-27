package de.hpi.ir.yahoogle;

import java.io.File;

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
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template'
															// with your search
															// engine's name,
															// i.e.
															// SearchEngineMyTeamName

	private static final String PATENT_LOCATION = "res/patents/";
	private YahoogleIndex index = new YahoogleIndex();

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex(String directory) {
	}

	@Override
	void index(String directory) {

		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();

			index.create();
			PatentParser handler = new PatentParser(index);
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			xr.setEntityResolver(handler);

			File patents = new File(PATENT_LOCATION);
			for (File file : patents.listFiles()) {
				if (isPatentFile(file)) {
					FileReader fr = new FileReader(PATENT_LOCATION + file.getName());
					xr.parse(new InputSource(fr));
				}
			}

			index.finish();
			index.write();

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

	private boolean isPatentFile(File file) {
		return !file.isDirectory() && file.getName().endsWith(".xml");
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		return false;
	}

	@Override
	boolean loadIndex(String directory) {
		return index.load();
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		Set<Integer> docNumbers = null;
		while (docNumbers == null) {
			if (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!YahoogleIndex.isStopword(token)) {
					docNumbers = index.find(token);
				}
			} else {
				return new ArrayList<String>();
			}
		}
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (!YahoogleIndex.isStopword(token)) {
				docNumbers.retainAll(index.find(token));
				// docNumbers.addAll(index.find(token));
			}
		}
		return index.matchInventionTitles(docNumbers);
	}

}
