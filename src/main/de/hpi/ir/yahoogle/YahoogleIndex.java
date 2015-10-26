package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String PATENTS_FILE = "patents.yahoogle";
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	private RandomAccessFile index;
	private Map<String, String> patents = new HashMap<String, String>();
	private List<YahoogleIndexPosting> posts = new ArrayList<YahoogleIndexPosting>();
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();

	public void add(Patent patent) {
		setInventionTitle(patent);
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = sanitize(tokenizer.nextToken());
			if (stopwords.contains(token)) {
				continue;
			}
			YahoogleIndexPosting posting = new YahoogleIndexPosting(token);
			posting.setDocNumber(patent.getDocNumber());
			posting.setPosition(i);
			posts.add(posting);
		}
		flush();
	}

	private void flush() {
		Collections.sort(posts);
		for (YahoogleIndexPosting post : posts) {
			post(post);
		}
		posts.clear();
	}

	public boolean create() {
		boolean status = deleteIfExists(POSTINGS_FILE)
				&& deleteIfExists(PATENTS_FILE)
				&& deleteIfExists(OFFSETS_FILE);
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		return status;
	}

	private boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

	public Set<String> find(String token) {
		token = sanitize(token);
		Set<String> docNumbers = new HashSet<String>();
		Long offset = tokenOffsets.get(token);
		if (offset != null) {
			try {
				while (offset != NO_NEXT_POSTING) {
					index.seek(offset);
					offset = index.readLong();
					docNumbers.add(index.readUTF());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docNumbers;
	}

	private String getInventionTitle(String docNumber) {
		return patents.get(docNumber);
	}

	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) loadObject(OFFSETS_FILE);
		patents = (Map<String, String>) loadObject(PATENTS_FILE);
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

	public ArrayList<String> match(Set<String> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (String docNumber : docNumbers) {
			results.add(getInventionTitle(docNumber));
		}
		return results;
	}

	private void post(YahoogleIndexPosting posting) {
		String token = posting.getToken();
		Long offset = tokenOffsets.get(token);
		try {
			if (offset == null) {
				tokenOffsets.put(token, index.length());
			} else {
				index.seek(offset);
				Long next;
				while ((next = index.readLong()) != NO_NEXT_POSTING) {
					index.seek(next);
					offset = next;
				}
				index.seek(offset);
				index.writeLong(index.length());
			}
			index.seek(index.length());
			index.writeLong(NO_NEXT_POSTING);
			index.writeUTF(posting.getDocNumber());
			index.writeInt(posting.getPosition());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	private void setInventionTitle(Patent patent) {
		patents.put(patent.getDocNumber(), patent.getInventionTitle());
	}

	public boolean write() {
		return writeObject(tokenOffsets, OFFSETS_FILE)
				&& writeObject(patents, PATENTS_FILE);
	}

	private boolean writeObject(Object o, String fileName) {
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

}
