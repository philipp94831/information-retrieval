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

	public static void main(String args[]) throws Exception {

		initialize(false);
		printResults(search("and resources and plurality"));

		// long start = System.currentTimeMillis();

		// myEngine.index(String directory)

		// long time = System.currentTimeMillis() - start;

		// System.out.print("Indexing Time:\t" + time + "\tms\n");

		// myEngine.loadIndex(String directory)

		// String query = "";

		// ArrayList <String> results = new ArrayList <> ();

		// results = myEngine.search(String query, int topK, int prf)

	}

	private static void printResults(ArrayList<String> results) {
		if (results.size() == 0) {
			System.out.println("No matches found");
		}
		for (String result : results) {
			System.out.println(result);
		}
	}

	private static ArrayList<String> search(String query) {
		long startTime = System.nanoTime();
		ArrayList<String> results = myEngine.search(query, 0, 0);
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for search: " + time + "ms");
		return results;
	}

	private static void initialize(boolean create) {
		long startTime = System.nanoTime();
		if (create) {
			myEngine.index("");
		} else {
			myEngine.loadIndex("");
		}
		long time = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Time for index creation: " + time + "ms");
	}

}
