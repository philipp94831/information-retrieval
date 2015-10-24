package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private RandomAccessFile index;
	private static final String FILENAME = "yahoogle.index";
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();
    
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
	
	public YahoogleIndex() {
		File f = new File(FILENAME);
		f.delete();
		try {
			index = new RandomAccessFile(FILENAME, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		return docNumbers;
	}
	
}
