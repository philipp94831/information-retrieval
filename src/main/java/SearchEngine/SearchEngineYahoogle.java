package SearchEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PatentIndexer;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.rm.bool.BooleanResult;
import de.hpi.ir.yahoogle.rm.ql.QLResult;
import de.hpi.ir.yahoogle.search.BooleanSearch;
import de.hpi.ir.yahoogle.search.QueryProcessor;
import de.hpi.ir.yahoogle.search.RelevantSearch;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

public class SearchEngineYahoogle extends SearchEngine {

	private static final Logger LOGGER = Logger
			.getLogger(SearchEngineYahoogle.class.getName());
	public static final int NUMBER_OF_THREADS = 4;
	private static final String QUERYLOG = "querylog.txt";
	public static final boolean USE_NDCG = false;

	private static double computeGain(int goldRank) {
		return 1 + Math.floor(10 * Math.pow(0.5, 0.1 * goldRank));
	}

	public static String getTeamDirectory() {
		return teamDirectory + "/";
	}

	private static String toGoogleQuery(String query) {
		return query.toLowerCase().replaceAll("\\snot\\s", " -")
				.replaceAll("^not\\s", "-");
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

	protected ArrayList<String> generateOutput(Collection<? extends Result> results, Map<Integer, String> snippets, String query) {
		ArrayList<String> output = new ArrayList<>();
		ArrayList<String> goldRanking = new ArrayList<>();
		if (USE_NDCG) {
			String googleQuery = toGoogleQuery(query);
			goldRanking = new WebFile().getGoogleRanking(googleQuery);
		}
		ArrayList<String> originalRanking = new ArrayList<>(
				results.stream().map(r -> Integer.toString(r.getDocNumber()))
						.collect(Collectors.toList()));
		int i = 1;
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			double ndcg = computeNdcg(goldRanking, originalRanking, i);
			output.add(String.format("%08d", docNumber) + "\t"
					+ index.getPatent(docNumber).getPatent().getInventionTitle()
					+ "\t" + ndcg + "\n" + snippets.get(docNumber));
			i++;
		}
		return output;
	}

	@Override
	public void index() {
		try {
			File patents = new File(dataDirectory);
			Queue<File> files = new ConcurrentLinkedQueue<>(
					Arrays.asList(patents.listFiles()));
			int size = files.size();
			List<PatentIndexer> threads = new ArrayList<>();
			for (int i = 0; i < Math.min(NUMBER_OF_THREADS, size); i++) {
				PatentIndexer indexer = new PatentIndexer(Integer.toString(i),
						files);
				indexer.start();
				threads.add(indexer);
			}
			try {
				for (PatentIndexer indexer : threads) {
					indexer.join();
				}
			} catch (InterruptedException e) {
				LOGGER.severe("Error joining indexer threads");
			}
			List<String> names = threads.stream().map(PatentIndexer::getNames)
					.flatMap(Collection::stream).collect(Collectors.toList());
			index = new Index(dataDirectory);
			index.create();
			index.mergeIndices(names);
			index.write();
			index.load();
			index.calculatePageRank();
		} catch (IOException e) {
			LOGGER.severe("Error indexing files");
		}
	}

	@Override
	protected boolean loadCompressedIndex() {
		return loadIndex();
	}

	@Override
	protected boolean loadIndex() {
		index = new Index(dataDirectory);
		try (BufferedReader br = new BufferedReader(new FileReader(
				SearchEngineYahoogle.getTeamDirectory() + QUERYLOG))) {
			index.load();
			String query;
			while ((query = br.readLine()) != null) {
				search(query, 10);
			}
			return true;
		} catch (IOException e) {
			LOGGER.severe("Error loading Index from disk");
		}
		return false;
	}

	@Override
	protected ArrayList<String> search(String query, int topK) {
		switch (QueryProcessor.getQueryType(query)) {
		case RELEVANT:
			return searchRelevant(query, topK);
		case BOOLEAN:
			return searchBoolean(query, topK);
		default:
			return new ArrayList<>();
		}
	}

	private ArrayList<String> searchBoolean(String query, int topK) {
		BooleanSearch s = new BooleanSearch(index, query);
		s.setTopK(topK);
		List<BooleanResult> results = s.search();
		return generateOutput(results, new SnippetGenerator(index)
				.generateSnippets(results, s.getPhrases()), s.getQuery());
	}

	private ArrayList<String> searchRelevant(String query, int topK) {
		RelevantSearch s = new RelevantSearch(index, query);
		s.setTopK(topK);
		List<QLResult> results = s.search();
		return generateOutput(results, new SnippetGenerator(index)
				.generateSnippets(results, s.getPhrases()), s.getQuery());
	}
}
