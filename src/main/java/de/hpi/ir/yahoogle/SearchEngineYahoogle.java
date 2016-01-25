package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

import de.hpi.ir.SearchEngine;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PatentReceiver;
import de.hpi.ir.yahoogle.parsing.PatentParser;
import de.hpi.ir.yahoogle.query.BooleanSearch;
import de.hpi.ir.yahoogle.query.LinkSearch;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.query.RelevantSearch;
import de.hpi.ir.yahoogle.query.Search;

public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template'
															// with your search
															// engine's name,
															// i.e.
															// SearchEngineMyTeamName

	private static final Logger LOGGER = Logger.getLogger(SearchEngineYahoogle.class.getName());

	public static String getTeamDirectory() {
		return teamDirectory;
	}

	private Index index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	protected void compressIndex() {
		index();
	}

	@Override
	protected Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		// See query.Search
		return null;
	}

	@Override
	public void index() {
		try {
			PatentReceiver receiver = new PatentReceiver();
			receiver.start();
			PatentParser handler = new PatentParser(receiver);
			File[] files = new File(dataDirectory).listFiles();
			for (File patentFile : files) {
				LOGGER.info(patentFile.getName());
				FileInputStream stream = new FileInputStream(patentFile);
				handler.setFileName(patentFile.getName());
				handler.parse(stream);
			}
			receiver.finish();
			index = new Index(dataDirectory);
			index.create();
			index.mergeIndices(receiver.getNames());
			index.write();
		} catch (IOException e) {
			LOGGER.severe("Error indexing files");
		} catch (XMLStreamException e) {
			LOGGER.severe("Error parsing XML");
		}
	}

	@Override
	protected boolean loadCompressedIndex() {
		return loadIndex();
	}

	@Override
	protected boolean loadIndex() {
		index = new Index(dataDirectory);
		try {
			index.load();
			index.warmUp();
			return true;
		} catch (IOException e) {
			LOGGER.severe("Error loading Index from disk");
		}
		return false;
	}

	@Override
	protected ArrayList<String> search(String query, int topK) {
		Search s;
		switch(QueryProcessor.getQueryType(query)) {
		case LINK:
			s = new LinkSearch(index, query);
			break;
		case RELEVANT:
			s = new RelevantSearch(index, query);
			break;
		case BOOLEAN:
			s = new BooleanSearch(index, query);
			break;
		default:
			return new ArrayList<>();
		}
		s.setTopK(topK);
		return s.search();
	}
}
