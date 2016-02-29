package SearchEngine;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

import de.hpi.ir.yahoogle.evaluation.Ndcg;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.partial.PatentIndexer;
import de.hpi.ir.yahoogle.retrieval.Result;
import de.hpi.ir.yahoogle.search.BooleanSearch;
import de.hpi.ir.yahoogle.search.QueryProcessor;
import de.hpi.ir.yahoogle.search.RelevantSearch;
import de.hpi.ir.yahoogle.search.Search;
import de.hpi.ir.yahoogle.search.SearchResult;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

public class SearchEngineYahoogle extends SearchEngine {

	private static final Logger LOGGER = Logger
			.getLogger(SearchEngineYahoogle.class.getName());
	public static final int NUMBER_OF_THREADS = 4;
	private static final String QUERYLOG = "querylog.txt";
	private static final boolean WARM_UP = false;

	public static String getTeamDirectory() {
		return teamDirectory + "/";
	}

	private static String patentFolder() {
		return dataDirectory + "/";
	}

	private Index index;

	public SearchEngineYahoogle() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void compressIndex() {
		index();
	}

	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		return new Ndcg(goldRanking, ranking).at(p);
	}

	private ArrayList<String> generateOutput(Collection<? extends Result> results, List<String> phrases) {
		ArrayList<String> output = new ArrayList<>();
		Map<Integer, String> snippets = new SnippetGenerator(index)
				.generateSnippets(results, phrases);
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			output.add(String.format("%08d", docNumber) + "\t"
					+ index.getPatent(docNumber).getInventionTitle() + "\t"
					+ "\n" + snippets.get(docNumber));
		}
		return output;
	}

	@Override
	void index() {
		try {
			File patents = new File(patentFolder());
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
			index = new Index(patentFolder());
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
	boolean loadCompressedIndex() {
		return loadIndex();
	}

	@Override
	boolean loadIndex() {
		index = new Index(patentFolder());
		try {
			index.load();
			if (WARM_UP) {
				warmUp();
			}
			return true;
		} catch (IOException e) {
			LOGGER.severe("Error loading Index from disk");
		}
		return false;
	}

	@Override
	ArrayList<String> search(String query, int topK) {
		return search(query, topK, false);
	}

	private ArrayList<String> search(String query, int topK, boolean silent) {
		Search<?> s;
		switch (QueryProcessor.getQueryType(query)) {
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
		SearchResult result = s.search();
		ArrayList<String> output = generateOutput(result.getResults(),
				s.getPhrases());
		if (!silent) {
			System.out.println(result.getResultSize() + " results returned");
		}
		return output;
	}

	private void warmUp() {
		LOGGER.info("Warming up...");
		try (BufferedReader br = new BufferedReader(
				new FileReader(getTeamDirectory() + QUERYLOG))) {
			String query;
			while ((query = br.readLine()) != null) {
				search(query, 10, true);
			}
		} catch (IOException e) {
			LOGGER.severe("Error reading query log");
		}
		LOGGER.info("Finished warm up");
	}
}
