package de.hpi.ir.yahoogle;

/**
 *
 * @author: Konstantina.Lazarid
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 */

import java.io.File;
import java.util.ArrayList;

/* The only change you should make in this file is to define your baseDirectory!!
*  for instance, C:/Users/myuser/Desktop/
*  In the constructor of your implementation class, namely "public SearchEngineMyTeamName()", you will super the constructor of this abstract class.
*  Then, as you can see in the "public SearchEngine()", a directory called "SearchEngineMyTeamName" will be created inside the baseDirectory.
*  This directory is defined in the "teamDirectory" variable.
*  It will contain all the files that you are using in your search engine.
*  Namely, all the files that are neccessary for your engine to run (i.e. your stopword list) and all the files that your program generates.
*/
public abstract class SearchEngine {

	// paths
	protected static String baseDirectory = "";
	/************* Define your baseDirectory here !! ******************/
	protected static String teamDirectory; // don't change this

	protected int prf;
	// we will need these later in the course
	protected int topK;

	public SearchEngine() {

		// the baseDirectory is already defined
		teamDirectory = baseDirectory + getClass().getSimpleName(); // creates
																	// SearchEngineMyTeamName
																	// directory
		new File(teamDirectory).mkdirs();
	}

	// contruct a compressed version of the index and save it in a file in the
	// teamDirectory
	abstract void compressIndex(String directory);

	// contruct your patent index and save it in a file in the teamDirectory
	abstract void index(String directory);

	// load the seeklist for the compressed index from the teamDirectory
	abstract boolean loadCompressedIndex(String directory);

	// load the index's seeklist from the teamDirectory
	abstract boolean loadIndex(String directory);

	// search the index for a given query and return the relevant patent titles
	// in an ArrayList of Strings
	abstract ArrayList<String> search(String query, int topK, int prf);

	// we will need this later in the course
	public void setPRF(int value) {
		prf = value;
	}

	// we will need this later in the course
	public void setTopK(int value) {
		topK = value;
	}

}
