package de.hpi.ir.yahoogle;

import java.util.ArrayList;

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

	private static final boolean CREATE_INDEX = true;
	private static final String[] EXECERSICE_10 = { "\"graph editor\"",
			"\"social trend\"", "fossil hydrocarbons",
			"physiological AND saline", "tires NOT pressure" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_12 = { "LinkTo:07920906",
			"LinkTo:07904949", "LinkTo:08078787",
			"LinkTo:07865308 AND 07925708", "LinkTo:07947864 AND 07947142" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_2 = { "selection", "device",
			"justify", "write" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_3 = { "file-system", "included",
			"storing" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_4 = { "comprises AND consists",
			"methods NOT invention", "data OR method", "prov* NOT free",
			"inc* OR memory", "\"the presented invention\"",
			"\"mobile devices\"" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_5 = { "processing", "computers",
			"\"mobile devices\"", "data" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_6 = { "digital", "rootkits",
			"network access" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_7 = { "access control",
			"computers", "data processing", "web servers",
			"vulnerability information", "computer-readable media" };
	@SuppressWarnings("unused")
	private static final String[] EXECERSICE_8 = { "access", "control",
			"image data", "program", "vulnerability", "\"mobile device\"" };
	@SuppressWarnings("unused")
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
		String[] queries = EXECERSICE_9;
		for (String query : queries) {
			printResults(search(query, 10), query);
		}
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
