package SearchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author: Your team name
 * @dataset: US patent grants : ipg files from
 *           http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 *          You can run your search engine using this file You can use/change
 *          this file during the development of your search engine. Any changes
 *          you make here will be ignored for the final test!
 */
public class SearchEngineTest {

	private static final boolean CREATE_INDEX = false;
	private static final String[] EXECERSICE_02 = { "selection", "device",
			"justify", "write" };
	private static final String[] EXECERSICE_03 = { "file-system", "included",
			"storing" };
	private static final String[] EXECERSICE_04 = { "comprises AND consists",
			"methods NOT invention", "data OR method", "prov* NOT free",
			"inc* OR memory", "\"the presented invention\"",
			"\"mobile devices\"" };
	private static final String[] EXECERSICE_05 = { "processing", "computers",
			"\"mobile devices\"", "data" };
	private static final String[] EXECERSICE_06 = { "digital", "rootkits",
			"network access", "digital #2", "rootkits #2",
			"network access #2" };
	private static final String[] EXECERSICE_07 = { "access control #2",
			"computers #2", "data processing #2", "web servers #2",
			"vulnerability information #2", "computer-readable media #2" };
	private static final String[] EXECERSICE_08 = { "access", "control",
			"image data", "program", "vulnerability", "\"mobile device\"" };
	private static final String[] EXECERSICE_09 = { "add-on module",
			"digital signature", "data processing", "\"a scanning\"" };
	private static final String[] EXECERSICE_10 = { "\"graph editor\"",
			"\"social trend\"", "fossil hydrocarbons",
			"physiological AND saline", "tires NOT pressure" };
	private static final String[] EXECERSICE_12 = { "LinkTo:07920906",
			"LinkTo:07904949", "LinkTo:08078787",
			"LinkTo:07865308 AND LinkTo:07925708",
			"LinkTo:07947864 AND LinkTo:07947142", "review guidelines",
			"on-chip OR OCV" };
	private static final String[] EXECERSICE_14 = { "Marker pen holder",
			"sodium polyphosphates", "\"ionizing radiation\"",
			"solar coronal holes", "patterns in scale-free networks",
			"\"nail polish\"", "\"keyboard shortcuts\"",
			"radiographic NOT ventilator", "multi-label AND learning",
			"LinkTo:07866385" };
	private static final Logger LOGGER = Logger
			.getLogger(SearchEngineTest.class.getName());
	private static SearchEngine myEngine = new SearchEngineYahoogle();
	private static final int NDCG_P = 10;
	private static final boolean PRINT_RESULTS = false;
	private static final boolean USE_NDCG = false;

	private static String[] allQueries() {
		List<String> all = new ArrayList<>();
		all.addAll(Arrays.asList(EXECERSICE_02));
		all.addAll(Arrays.asList(EXECERSICE_03));
		all.addAll(Arrays.asList(EXECERSICE_04));
		all.addAll(Arrays.asList(EXECERSICE_05));
		all.addAll(Arrays.asList(EXECERSICE_06));
		all.addAll(Arrays.asList(EXECERSICE_07));
		all.addAll(Arrays.asList(EXECERSICE_08));
		all.addAll(Arrays.asList(EXECERSICE_09));
		all.addAll(Arrays.asList(EXECERSICE_10));
		all.addAll(Arrays.asList(EXECERSICE_12));
		all.addAll(Arrays.asList(EXECERSICE_14));
		String[] result = new String[all.size()];
		return all.toArray(result);
	}

	private static double getNdcg(String query, ArrayList<String> ranking, int p) {
		ArrayList<String> goldRanking = new WebFile()
				.getGoogleRanking(toGoogleQuery(query));
		return myEngine.computeNdcg(goldRanking, ranking, p);
	}

	private static void initialize(boolean create) {
		if (create) {
			long startTime = System.nanoTime();
			LOGGER.info("Indexing...");
			myEngine.index();
			long time = (System.nanoTime() - startTime) / 1000000;
			LOGGER.info("Time for index creation: " + time + "ms");
		}
		long startTime = System.nanoTime();
		LOGGER.info("Loading index...");
		myEngine.loadCompressedIndex();
		long time = (System.nanoTime() - startTime) / 1000000;
		LOGGER.info("Time for index loading: " + time + "ms");
		System.out.println();
	}

	public static void main(String args[]) throws Exception {
		initialize(CREATE_INDEX);
		String[] queries = allQueries();
		for (String query : queries) {
			printResults(search(query, 10), query);
		}
		LOGGER.info("finished");
	}

	private static void printResults(ArrayList<String> results, String query) {
		System.out.println(query);
		if (PRINT_RESULTS) {
			if (results.isEmpty()) {
				System.out.println("No matches found");
			}
			results.forEach(System.out::println);
		}
		if (USE_NDCG) {
			System.out.println(
					"NDCG@" + NDCG_P + ": " + getNdcg(query, results, NDCG_P));
		}
		System.out.println();
	}

	private static ArrayList<String> search(String query, int topK) {
		LOGGER.finer("Searching...");
		long startTime = System.nanoTime();
		ArrayList<String> result = myEngine.search(query, topK);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for search: " + time + "ms");
		return result;
	}

	private static String toGoogleQuery(String query) {
		return query.toLowerCase().replaceAll("\\snot\\s", " -")
				.replaceAll("^not\\s", "-");
	}
}
