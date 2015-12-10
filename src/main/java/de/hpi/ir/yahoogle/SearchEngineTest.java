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
		initialize("patents/", true);
		System.out.println("==============================");
		String[] queries = { "mobile device" };
		for (String query : queries) {
			printResults(search(query, 10, 2), query);
		}
		// long start = System.currentTimeMillis();
		// myEngine.index(String directory)
		// long time = System.currentTimeMillis() - start;
		// System.out.print("Indexing Time:\t" + time + "\tms\n");
		// myEngine.loadIndex(String directory)
		// String query = "";
		// ArrayList <String> results = new ArrayList <> ();
		// results = myEngine.search(String query, int topK, int prf)
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

	private static ArrayList<String> search(String query, int topK, int prf) {
		// System.out.println("Searching...");
		long startTime = System.nanoTime();
		ArrayList<String> results = myEngine.search(query, topK, prf);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for search: " + time + "ms");
		return results;
	}
}
