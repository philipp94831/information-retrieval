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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.hpi.ir.yahoogle.index.Index;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

	private Index index = new Index();

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex(String directory) {
		index(directory);
	}
	
	public static String getTeamDirectory() {
		return teamDirectory;
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
			
			ZipFile zipFile = new ZipFile(directory);
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();

		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        System.out.println(entry.getName());
		        InputStream stream = zipFile.getInputStream(entry);
		        xr.parse(new InputSource(stream));
		    }
		    
		    zipFile.close();

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
		List<String> queryPlan = processQuery(query);
		if(queryPlan.isEmpty()) {
			return new ArrayList<String>();
		}
		if(queryPlan.size() == 1 && !queryPlan.get(0).contains("*")) {
			List<String> phrases = extractPhrases(queryPlan.get(0));
			List<Integer> results = index.findRelevant(phrases, topK);
			return index.matchInventionTitles(results);
		}
		Set<Integer> docNumbers = index.getAllDocNumbers();
		Operator operator = Operator.AND;
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

	private List<String> extractPhrases(String partialQuery) {
		List<String> phrases = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(partialQuery);
		StringBuffer buffer = new StringBuffer();
		boolean inPhrase = false;
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(token.startsWith("'")) {
				inPhrase = true;
			}
			if(token.endsWith("'")) {
				inPhrase = false;
			}
			buffer.append(" " + token.replaceAll("'", ""));
			if(!inPhrase) {
				phrases.add(buffer.toString());
				buffer = new StringBuffer();
			}
		}
		return phrases;
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
				String cleanedToken = token.replaceAll("'", "");
				if(!StopWordList.isStopword(cleanedToken)) {
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
