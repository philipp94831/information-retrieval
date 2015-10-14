package de.hpi.ir.searchengine;

/**
 *
 * @author: Your team name
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

import java.util.ArrayList;



public class SearchEngineTemplate extends SearchEngine {

    // Replace 'Template' with your search engine name, i.e. SearchEngineMyTeamName
    
    public SearchEngineTemplate() {
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
    }

    @Override
    boolean loadIndex(String directory) {
        return false;
    }
    
    @Override
    void compressedIndex(String directory) {
    }

    @Override
    boolean loadCompressedIndex(String directory) {
        return false;
    }
    
    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        return null;
    }
    
    @Override
    ArrayList<String> booleanSearch(String query, int topK, int prf) {
        return null; 
    }

    @Override
    Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int ndcgAt) {
        return null;
    }

    
}
