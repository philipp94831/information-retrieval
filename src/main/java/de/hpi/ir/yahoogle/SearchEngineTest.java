package de.hpi.ir.yahoogle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hpi.ir.yahoogle.index.PatentIndex;

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
@SuppressWarnings("unused")
public class SearchEngineTest {

	private static final boolean CREATE_INDEX = true;
	private static final String[] EXECERSICE_10 = { "\"graph editor\"",
			"\"social trend\"", "fossil hydrocarbons",
			"physiological AND saline", "tires NOT pressure" };
	private static final String[] EXECERSICE_12 = { "LinkTo:07920906",
			"LinkTo:07904949", "LinkTo:08078787",
			"LinkTo:07865308 AND 07925708", "LinkTo:07947864 AND 07947142",
			"review guidelines", "on-chip OR OCV" };
	private static final String[] EXECERSICE_2 = { "selection", "device",
			"justify", "write" };
	private static final String[] EXECERSICE_3 = { "file-system", "included",
			"storing" };
	private static final String[] EXECERSICE_4 = { "comprises AND consists",
			"methods NOT invention", "data OR method", "prov* NOT free",
			"inc* OR memory", "\"the presented invention\"",
			"\"mobile devices\"" };
	private static final String[] EXECERSICE_5 = { "processing", "computers",
			"\"mobile devices\"", "data" };
	private static final String[] EXECERSICE_6 = { "digital", "rootkits",
			"network access" };
	private static final String[] EXECERSICE_7 = { "access control",
			"computers", "data processing", "web servers",
			"vulnerability information", "computer-readable media" };
	private static final String[] EXECERSICE_8 = { "access", "control",
			"image data", "program", "vulnerability", "\"mobile device\"" };
	private static final String[] EXECERSICE_9 = { "add-on module",
			"digital signature", "data processing", "\"a scanning\"" };
	private static SearchEngineYahoogle myEngine = new SearchEngineYahoogle();

	private static void initialize(boolean create) {
		long startTime = System.nanoTime();
		if (create) {
			System.out.println("Indexing...");
			myEngine.index();
		}
		System.out.println("Loading index...");
		myEngine.loadIndex();
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for index creation: " + time + "ms");
	}

	public static void main(String args[]) throws Exception {
		initialize(CREATE_INDEX);
		System.out.println("==============================");
		String[] queries = EXECERSICE_10;
		for (String query : queries) {
			printResults(search(query, 15), query);
		}
	}

	private static String[] allQueries() {
		List<String> all = new ArrayList<>();
		all.addAll(Arrays.asList(EXECERSICE_2));
		all.addAll(Arrays.asList(EXECERSICE_3));
		all.addAll(Arrays.asList(EXECERSICE_4));
		all.addAll(Arrays.asList(EXECERSICE_5));
		all.addAll(Arrays.asList(EXECERSICE_6));
		all.addAll(Arrays.asList(EXECERSICE_7));
		all.addAll(Arrays.asList(EXECERSICE_8));
		all.addAll(Arrays.asList(EXECERSICE_9));
		all.addAll(Arrays.asList(EXECERSICE_10));
		all.addAll(Arrays.asList(EXECERSICE_12));
		String[] result = new String[all.size()];
		return all.toArray(result);
	}

	private static void printResults(ArrayList<String> results, String query) {
		System.out.println(query);
		if (results.size() == 0) {
			System.out.println("No matches found");
		}
		results.forEach(System.out::println);
		// System.out.println(results.size() + " Results returned");
		System.out.println();
	}

	private static ArrayList<String> search(String query, int topK) {
		// System.out.println("Searching...");
		long startTime = System.nanoTime();
		ArrayList<String> results = myEngine.search(query, topK);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for search: " + time + "ms");
		return results;
	}
}
