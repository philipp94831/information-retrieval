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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.SearchEngine;
import de.hpi.ir.WebFile;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PatentReceiver;
import de.hpi.ir.yahoogle.parsing.PatentParser;
import de.hpi.ir.yahoogle.query.BooleanSearch;
import de.hpi.ir.yahoogle.query.LinkSearch;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.query.RelevantSearch;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.rm.bool.BooleanResult;
import de.hpi.ir.yahoogle.rm.ql.QLResult;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

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
		double originalDcg = 0.0;
		double goldDcg = 0.0;
		for (int i = 0; i < p; i++) {
			String originalRank = ranking.get(i);
			int goldRank = goldRanking.indexOf(originalRank) + 1;
			double originalGain = goldRank == 0 ? 0 : computeGain(goldRank);
			double goldGain = computeGain(i + 1);
			if (i == 0) {
				originalDcg = originalGain;
				goldDcg = goldGain;
			} else {
				originalDcg += originalGain * Math.log(2) / Math.log(i + 1);
				goldDcg += goldGain * Math.log(2) / Math.log(i + 1);
			}
		}
		return originalDcg / goldDcg;
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

	private static double computeGain(int goldRank) {
		return 1 + Math.floor(10 * Math.pow(0.5, 0.1 * goldRank));
	}

	private static String toGoogleQuery(String query) {
		return query.toLowerCase().replaceAll("\\snot\\s", " -").replaceAll("^not\\s", "-");
	}

	protected ArrayList<String> generateOutput(Collection<? extends Result> results, Map<Integer, String> snippets,
			String query) {
		ArrayList<String> output = new ArrayList<>();
		String googleQuery = toGoogleQuery(query);
		ArrayList<String> goldRanking = new WebFile().getGoogleRanking(googleQuery);
		ArrayList<String> originalRanking = new ArrayList<>(
				results.stream().map(r -> Integer.toString(r.getDocNumber())).collect(Collectors.toList()));
		int i = 1;
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			double ndcg = computeNdcg(goldRanking, originalRanking, i);
			output.add(
					String.format("%08d", docNumber) + "\t" + index.getPatent(docNumber).getPatent().getInventionTitle()
							+ "\t" + ndcg + "\n" + snippets.get(docNumber));
			i++;
		}
		return output;
	}

	protected ArrayList<String> generateSlimOutput(Collection<? extends Result> r) {
		ArrayList<String> results = new ArrayList<>();
		for (Result result : r) {
			results.add(String.format("%08d", result.getDocNumber()) + "\t"
					+ index.getPatent(result.getDocNumber()).getPatent().getInventionTitle());
		}
		return results;
	}

	@Override
	protected ArrayList<String> search(String query, int topK) {
		switch(QueryProcessor.getQueryType(query)) {
		case LINK:
			return searchLinks(query, topK);
		case RELEVANT:
			return searchRelevant(query, topK);
		case BOOLEAN:
			return searchBoolean(query, topK);
		default:
			return new ArrayList<>();
		}
	}

	private ArrayList<String> searchLinks(String query, int topK) {
		LinkSearch s = new LinkSearch(index, query);
		s.setTopK(topK);
		List<BooleanResult> results = s.search();
		return generateSlimOutput(results);
	}

	private ArrayList<String> searchRelevant(String query, int topK) {
		RelevantSearch s = new RelevantSearch(index, query);
		s.setTopK(topK);
		List<QLResult> results = s.search();
		return generateOutput(results, new SnippetGenerator(index).generateSnippets(results, s.getPhrases()), s.getQuery());
	}

	private ArrayList<String> searchBoolean(String query, int topK) {
		BooleanSearch s = new BooleanSearch(index, query);
		s.setTopK(topK);
		List<QLResult> results = s.search();
		return generateOutput(results, new SnippetGenerator(index).generateSnippets(results, s.getPhrases()), s.getQuery());
	}
}
