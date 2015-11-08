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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

	private YahoogleIndex index = new YahoogleIndex();

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex(String directory) {
		index(directory);
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

			File patents = new File(directory);
			for (File file : patents.listFiles()) {
				if (isPatentFile(file)) {
					FileReader fr = new FileReader(directory + file.getName());
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
		return loadIndex(directory);
	}

	@Override
	boolean loadIndex(String directory) {
		return index.load();
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		Set<Integer> docNumbers = index.getAllDocNumbers();
		Operator operator = Operator.AND;
		List<String> queryPlan = processQuery(query);
		if(queryPlan.isEmpty()) {
			return new ArrayList<String>();
		}
		for (String phrase : queryPlan) {
			switch(phrase.toLowerCase()) {
			case "and":
				operator = Operator.AND;
				break;
			case "or":
				operator = Operator.OR;
				break;
			case "not":
				operator = Operator.NOT;
				break;
			default:
				Set<Integer> result = index.find(phrase);
				switch (operator) {
				case AND:
					docNumbers.retainAll(result);
					break;
				case OR:
					docNumbers.addAll(result);
					break;
				case NOT:
					docNumbers.removeAll(result);
					break;
				}
				break;
			}
		}
		return index.matchInventionTitles(docNumbers);
	}

	private List<String> processQuery(String query) {
		StringTokenizer tokenizer = new StringTokenizer(query);
		List<String> queryPlan = new ArrayList<String>();
		StringBuffer phrase = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			boolean checkEmpty = false;
			switch(token.toLowerCase()) {
			case "and":
			case "or":
				checkEmpty = true;
			case "not":
				if(phrase.length() > 0) {
					queryPlan.add(phrase.toString());
					phrase = new StringBuffer();
				} else {
					if(queryPlan.size() > 0) {
						queryPlan.remove(queryPlan.size() - 1);
					}
				}
				if(!(checkEmpty && queryPlan.isEmpty())) {
					queryPlan.add(token);
				}
				break;
			default:
				if(!StopWordList.isStopword(token)) {
					phrase.append(" " + token);
				}
				break;
			}
		}
		if(phrase.length() > 0) {
			queryPlan.add(phrase.toString());
		} else {
			if(queryPlan.size() > 0) {
				queryPlan.remove(queryPlan.size() - 1);
			}
		}
		return queryPlan;
	}

}
