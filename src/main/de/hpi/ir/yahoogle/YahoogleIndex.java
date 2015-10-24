package de.hpi.ir.yahoogle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
	private StopWordList stopwords;

	boolean write() {
		return writeObject(tokenOffsets, OFFSETS_FILE)
				&& writeObject(patents, PATENTS_FILE);
	}

	public YahoogleIndex() {
		stopwords = new StopWordList("res/stopwords.txt");
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

	private Object loadObject(String fileName) {
		Object o;
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream oin = new ObjectInputStream(fin);
			o = oin.readObject();
			oin.close();
			fin.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return o;
	}

	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) loadObject(OFFSETS_FILE);
		patents = (Set<Patent>) loadObject(PATENTS_FILE);
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
		token = stemmer.stem(token.toLowerCase());
		token = token.replaceAll("\\W", "");
		if(stopwords.contains(token)) return;
		Long offset = tokenOffsets.get(token);
		try {
			if (offset == null) {
				tokenOffsets.put(token, index.length());
			} else {
				while (true) {
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
		} catch (IOException e) {

		}
	}

	public Set<String> find(String token) {
		Set<String> docNumbers = new HashSet<String>();
		Long offset = tokenOffsets.get(token);
		if (offset == null) {
			return docNumbers;
		}
		try {
			while (offset != -1) {
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

	public void add(Patent patent) {
		patent.setPatentAbstract(null);
		patents.add(patent);
	}

	public ArrayList<String> match(Set<String> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Patent patent : patents) {
			if (docNumbers.contains(patent.getDocNumber())) {
				results.add(patent.toString());
			}
		}
		return results;
	}

}
