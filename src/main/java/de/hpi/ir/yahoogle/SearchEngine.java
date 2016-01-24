package de.hpi.ir.yahoogle;

/**
 *
 * @author: Konstantina.Lazarid
 * @dataset: US patent utility grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html from 2011 to 2015
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 */
import java.util.ArrayList;

/* The only changes you should make in this file is to define your baseDirectory and dataDirectory!!
*  for instance, C:/Users/myUser/Desktop/
*  In the constructor of your implementation class, namely "public SearchEngineMyTeamName()", you will super the constructor of this abstract class.
*  Then, as you can see in the "public SearchEngine()", a directory called "C:/Users/myUser/Desktop/SearchEngineMyTeamName" will be created inside the baseDirectory.
*  This directory is defined in the "teamDirectory" variable.
*  It will contain all the files that you are using in your search engine.
*  Namely, all the files that are necessary for your engine to run (i.e. your stopWord list) and all the files that your program generates.
*/
public abstract class SearchEngine {

	// paths
	protected static String baseDirectory = ""; // directory containing all xml
												// files from PatentData.zip :
	protected static String dataDirectory = "patents/";
	/************* Define your baseDirectory here !! ******************/
	protected static String teamDirectory; // don't change this protected int
											// prf; // whether to use prf and
											// how many patents to consider
											// (defined in the query with the
											// symbol '#')
	/************* Define your dataDirectory here !! ******************/
	protected int topK; // how many patents to return

	public SearchEngine() {
		// the baseDirectory is already defined
		teamDirectory = baseDirectory + getClass().getSimpleName(); // creates
																	// SearchEngineMyTeamName
																	// directory
		// new File(teamDirectory).mkdirs();
	}

	// construct a compressed version of the index and save it in a file in the
	// teamDirectory
	abstract void compressIndex();

	// compute the NDCG metric using your ranking and the google ranking for a
	// given query (the gold ranking method returns IDs, so the ranking
	// arraylist should also contain IDs)
	abstract Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p);

	// contruct your patent index and save it in a file in the teamDirectory
	abstract void index();

	// load the seeklist for the compressed index from the teamDirectory
	abstract boolean loadCompressedIndex();

	// load the index's seeklist from the teamDirectory
	abstract boolean loadIndex();

	// search the index for a given query and return the relevant patents (with
	// your improved visualization and the NDCG values) in an ArrayList of
	// Strings
	abstract ArrayList<String> search(String query, int topK);
}