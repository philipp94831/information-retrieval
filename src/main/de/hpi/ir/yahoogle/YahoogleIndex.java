package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private RandomAccessFile index;
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String PATENTS_FILE = "patents.yahoogle";
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();
	private Set<Patent> patents = new HashSet<Patent>();
    
    boolean write() {
    	return
    			writeObject(tokenOffsets, OFFSETS_FILE) &&
    			writeObject(patents, PATENTS_FILE);
    }
    
    boolean writeObject(Object o, String fileName) {
    	try {
			FileOutputStream fout = new FileOutputStream(fileName);
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(o);
			oout.close();
			fout.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    	
    	return true;
    }
    
    private boolean loadOffsets() {
    	try {
			FileInputStream fin = new FileInputStream(OFFSETS_FILE);
			ObjectInputStream oin = new ObjectInputStream(fin);
			tokenOffsets = (Map<String, Long>) oin.readObject();
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
	
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		loadOffsets();
		loadPatents();
		return true;
	}
	
	private boolean loadPatents() {
		try {
			FileInputStream fin = new FileInputStream(PATENTS_FILE);
			ObjectInputStream oin = new ObjectInputStream(fin);
			patents = (Set<Patent>) oin.readObject();
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

	public boolean create() {
		File f = new File(POSTINGS_FILE);
		f.delete();
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;
	}

	public void add(String token, YahoogleIndexPosting posting) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		token = stemmer.stem(token);
		token = token.replaceAll("\\W", "");
		Long offset = tokenOffsets.get(token);
		try {
			if(offset == null) {
				tokenOffsets.put(token, index.length());
			} else {
				while(true) {
					index.seek(offset);
					Long next = index.readLong();
					if (next == -1) {
						index.seek(offset);
						index.writeLong(index.length());
						break;
					}
					offset = next;
				}
			}
			index.seek(index.length());
			index.writeLong(-1);
			index.writeUTF(posting.getDocNumber());
			index.writeInt(posting.getPosition());
		} catch(IOException e) {
			
		}
	}
	
	public Set<String> getDocNumbers(String token) {
		Set<String> results = new HashSet<String>();
		Set<String> docNumbers = new HashSet<String>();
		Long offset = tokenOffsets.get(token);
		if(offset == null) {
			return docNumbers;
		}
		try {
			while(offset != -1) {
				index.seek(offset);
				offset = index.readLong();
				docNumbers.add(index.readUTF());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Patent patent : patents) {
			if (docNumbers.contains(patent.getDocNumber())) {
				results.add(patent.getInventionTitle());
			}
		}
		return results;
	}

	public void add(Patent patent) {
		patents.add(patent);
	}
	
}