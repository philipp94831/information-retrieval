
package de.hpi.ir.searchengine;

/**
 *
 * @author: Konstantina.Lazarid
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 */

import java.io.File;
import java.util.ArrayList;

// Don't change this file!!
public abstract class SearchEngine {

        // paths
		protected static String baseDirectory;
        protected static String teamDirectory;
		protected String resultsDirectory; // search engine results
		protected static String indexDirectory;
		protected String logFiles;
        
        protected boolean compression; // dictionary compression
        protected int machine; // execution machine
        
        protected WebFile wf; // for crawling the google ranking
        
        protected int topK; // for limiting the search engine results
        protected int prf; // for the pseudo relevance feedback implementation
        
        /*
        for each team: 
        "C:/../teamX"
        "C:/.../teamX/results/"
        "C:/.../teamX/logs/timestampK.log"
        "C:/.../teamX/indices/index.txt"
        "C:/.../teamX/indices/seeklist.txt"
        */
        
	public SearchEngine() {
		
            if (machine == 0) {
                baseDirectory = "C:/Users/Konstantina.Lazarid/Dropbox/Konna_IRWS/IRWS/IRWS/data/";
            } else if (machine == 1) {
                baseDirectory = "C:/Users/konstantina/Dropbox/Konna_IRWS/IRWS/IRWS/data/";
            }
            // for each team
            teamDirectory = baseDirectory + getClass().getSimpleName();
            // directory to store index and result logs
            new File(teamDirectory).mkdirs();
            indexDirectory = teamDirectory + "/indices/";
            new File(indexDirectory).mkdirs();
            logFiles = teamDirectory + "/logs/" + System.currentTimeMillis() + ".log";
            new File(logFiles).mkdirs();
            // directory to store query results
            resultsDirectory = teamDirectory + "/results/";
            new File(resultsDirectory).mkdirs();
	}

	Double indexWrapper(){
		
            return null;
	}

	Double[] searchWrapper(String query, int topK, int prf){
		
		return null;
	}
	
        // contruct your patent index and save it in a file
        abstract void index(String directory);
        
        // load your index from the file where you stored it (load its seek list)
		abstract boolean loadIndex(String directory);
        
        // contruct a compressed version of the index
        abstract void compressedIndex(String directory);

        // load it in main memory (load its seek list)
        abstract boolean loadCompressedIndex(String directory);

        // search the index for a given query and return the patent titles in an ArrayList of Strings
		abstract ArrayList<String> search(String query, int topK, int prf);
        
        // search the index for a query that contains : AND, OR, NOT (and *) and return the patent titles in an ArrayList of Strings
        abstract ArrayList<String> booleanSearch(String query, int topK, int prf);

        // get the gold ranking from google (already implemented in WebFile class) and compute your engine's quality compared to this ranking
		abstract Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> myRanking, int at);
        
        // helpers
        public String getBaseDirectory(){
            return(baseDirectory);
        }
        
        public String getIndexDirectory(){
            return(indexDirectory);
        }
        
        public String getResultsDirectory(){
            return(resultsDirectory);
        }
        
        public void enableCompression(){
            compression = true;
        }
        
        public void setMachine(int mainMachine){
            machine = mainMachine;
        }
        
        public void setTopK(int val){
            topK = val;
        }
        
        public void setPRF(int val){
            prf = val;
        }
        
}
