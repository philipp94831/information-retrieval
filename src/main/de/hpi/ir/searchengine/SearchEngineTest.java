package de.hpi.ir.searchengine;

/**
 *
 * @author: Konstantina.Lazarid
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * You can run your search engine using this file
 * You can use/change this file for development.
 * Any changes you make here will be ignored for the final test!!
 */

import java.util.ArrayList;

public class SearchEngineTest {
    
//    private static MySAXApp msa; // SAX parser application
    private static int machine; // 0 server, 1 local
    private static SearchEngine MyEngine; // patent engine
    private static int topK = 10; // result set size
    
    public static void main(String args[]) throws Exception {

        /*set your machine*/
            // MyEngine.setMachine(int mainMachine)
        
        /*configure the index*/
            // how many indices will you merge/sort per step etc...
        
        /*configure the parser*/
        
        /*create your index*/
            // MyEngine.index(String directory)
        
        /*load it in memory*/
            // MyEngine.loadIndex(String directory)
        
            String query = "";
        
        /*limit the search engine results that you print*/
            MyEngine.setTopK(topK);
        
        /*search for a given query*/
            ArrayList <String> results = new ArrayList <> ();
            // results = MyEngine.search(String query, int topK, int prf)
        
        /*get the google ranking for a given query*/
            WebFile wf = new WebFile();
            wf.getGoogleRanking(query);
         // wf.enableCaching();
         // wf.storeResults(results, query);
		   
        /*evaluate your engine*/
    }

}
