package de.hpi.ir.yahoogle;

/**
 *
 * @author: Yahoogle
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

import org.lemurproject.kstem.KrovetzStemmer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class SearchEngineYahoogle extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

    private static final String FILENAME = "yahoogle.index";
	private YahoogleIndex index;
	
    public SearchEngineYahoogle() {
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
    	index = indexPatents();
    	writeIndex(directory);
    }
    
    boolean writeIndex(String directory) {
    	try {
			FileOutputStream fout = new FileOutputStream(FILENAME);
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(index);
			oout.close();
			fout.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    	
    	return true;
    }

    @Override
    boolean loadIndex(String directory) {
        try {
			FileInputStream fin = new FileInputStream(FILENAME);
			ObjectInputStream oin = new ObjectInputStream(fin);
			index = (YahoogleIndex) oin.readObject();
			oin.close();
			fin.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    	return true;
    }
    
    @Override
    void compressIndex(String directory) {
    }

    @Override
    boolean loadCompressedIndex(String directory) {
        return false;
    }
    
    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        KrovetzStemmer stemmer = new KrovetzStemmer();
    	query = stemmer.stem(query);
    	Set<String> docNumbers = index.getDocNumbers(query);
    	for(String docNumber : docNumbers) {
    		System.out.println(docNumber);
    	}
        return null;
    }
    
    YahoogleIndex indexPatents() {
    	String fileName = "res/testData.xml";
    	
    	try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
	    	
			PatentHandler handler = new PatentHandler();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			
			FileReader r = new FileReader(fileName);
			xr.parse(new InputSource(r));
			
			return handler.getIndex();
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    	
    }
    
}
