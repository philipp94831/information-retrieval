package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final int FLUSH_THRESHOLD = 1000;
	private static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String PATENTS_FILE = "patents.yahoogle";
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final int POST_SIZE = Integer.BYTES +  Short.BYTES;
	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	private RandomAccessFile index;
	private int queue_size = 0;
	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private Map<String, List<YahoogleIndexPosting>> posts = new TreeMap<String, List<YahoogleIndexPosting>>();
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();

	public void add(Patent patent) {
		setInventionTitle(patent);
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for (short i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = sanitize(tokenizer.nextToken());
			if (stopwords.contains(token)) {
				continue;
			}
			YahoogleIndexPosting posting = new YahoogleIndexPosting(token);
			posting.setDocNumber(patent.getDocNumber());
			posting.setPosition(i);
			queue(posting);
		}
	}
	
	public boolean create() {
		boolean status = deleteIfExists(POSTINGS_FILE)
				&& deleteIfExists(PATENTS_FILE) && deleteIfExists(OFFSETS_FILE);
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

	public Set<Integer> find(String token) {
		token = sanitize(token);
		Set<Integer> docNumbers = new HashSet<Integer>();
		Long offset = tokenOffsets.get(token);
		if (offset != null) {
			try {
				while (offset != NO_NEXT_POSTING) {
					index.seek(offset);
					offset = index.readLong();
					short size = index.readShort();
					byte[] b = new byte[size * POST_SIZE];
					index.readFully(b);
					for (int i = 0; i < size; i++) {
						ByteBuffer bb = ByteBuffer.wrap(b, i * POST_SIZE, Integer.BYTES);
						docNumbers.add(bb.getInt());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docNumbers;
	}

	public void finish() {
		flush();
	}

	private void flush() {
		for (Map.Entry<String, List<YahoogleIndexPosting>> entry : posts.entrySet()) {
			postBlock(entry.getKey(), entry.getValue());
		}
		posts.clear();
		queue_size = 0;
	}

	private String getInventionTitle(Integer docNumber) {
		return patents.get(docNumber).getInventionTitle();
	}

	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			index = new RandomAccessFile(POSTINGS_FILE, "rw");
		} catch (FileNotFoundException e) {
			return false;
		}
		tokenOffsets = (Map<String, Long>) loadObject(OFFSETS_FILE);
		patents = (Map<Integer, PatentResume>) loadObject(PATENTS_FILE);
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

	public ArrayList<String> match(Set<Integer> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Integer docNumber : docNumbers) {
			results.add(getInventionTitle(docNumber));
		}
		return results;
	}

	private void post(YahoogleIndexPosting posting) throws IOException {
		index.seek(index.length());
		int docNumber = posting.getDocNumber();
		index.writeInt(docNumber);
		index.writeShort(posting.getPosition());
	}

	private void postBlock(String token, List<YahoogleIndexPosting> postList) {
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
			index.writeShort(postList.size());
			for (YahoogleIndexPosting post : postList) {
				post(post);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void queue(YahoogleIndexPosting posting) {
		String token = posting.getToken();
		if(posts.get(token) == null) {
			posts.put(token, new ArrayList<YahoogleIndexPosting>());
		}
		posts.get(token).add(posting);
		queue_size++;
		if (queue_size > FLUSH_THRESHOLD) {
			flush();
		}
	}

	private String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	private void setInventionTitle(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		patents.put(patent.getDocNumber(), resume);
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
