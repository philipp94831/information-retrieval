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
	private static SearchEngineYahoogle myEngine = new SearchEngineYahoogle();

	private static void initialize(String directory, boolean create) {
		long startTime = System.nanoTime();
		if (create) {
			System.out.println("Indexing...");
			myEngine.index(directory);
		}
		System.out.println("Loading index...");
		myEngine.loadIndex(directory);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for index creation: " + time + "ms");
	}

	public static void main(String args[]) throws Exception {
		initialize("patents/", CREATE_INDEX);
		System.out.println("==============================");
		String[] queries = { "\"graph editor\"", "\"social trend\"", "fossil hydrocarbons", "physiological AND saline", "tires NOT pressure" };
		for (String query : queries) {
			printResults(search(query, 20), query);
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
		ArrayList<String> results = myEngine.search(query, topK, 0);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for search: " + time + "ms");
		return results;
	}
}
