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
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final int FLUSH_COUNTER_THRESHOLD = 100;
	private static final long FLUSH_MEM_THRESHOLD = 20000000;
	private static final long NO_NEXT_POSTING = -1;
	private static final String OFFSETS_FILE = "offsets.yahoogle";
	private static final String PATENTS_FILE = "patents.yahoogle";
	private static final int POST_SIZE = Integer.BYTES + Short.BYTES;
	private static final String POSTINGS_FILE = "postings.yahoogle";
	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);
	private static final String TMP_POSTINGS_FILE = "tmp.postings.yahoogle";

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	private int flushCounter = 0;
	private Map<Integer, PatentResume> patents = new HashMap<Integer, PatentResume>();
	private Map<String, List<YahoogleIndexPosting>> posts = new TreeMap<String, List<YahoogleIndexPosting>>();
	private RandomAccessFile tmp_index, index;
	private Map<String, Long> tmp_tokenOffsets = new HashMap<String, Long>();
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
			YahoogleIndexPosting posting = new YahoogleIndexPosting();
			posting.setDocNumber(patent.getDocNumber());
			posting.setPosition(i);
			queue(token, posting);
		}
	}

	public boolean create() {
		boolean status = deleteIfExists(TMP_POSTINGS_FILE)
				&& deleteIfExists(POSTINGS_FILE)
				&& deleteIfExists(PATENTS_FILE) && deleteIfExists(OFFSETS_FILE);
		try {
			tmp_index = new RandomAccessFile(TMP_POSTINGS_FILE, "rw");
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
				index.seek(offset);
				int size = index.readInt();
				byte[] b = new byte[size * POST_SIZE];
				index.readFully(b);
				for (int i = 0; i < size; i++) {
					ByteBuffer bb = ByteBuffer.wrap(b, i * POST_SIZE,
							Integer.BYTES);
					docNumbers.add(bb.getInt());
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
		reorganize();
	}

	private void flush() {
		for (String token : posts.keySet()) {
			flush(token);
		}
		flushCounter = 0;
	}

	private void flush(String token) {
		postBlock(token, posts.get(token));
		posts.get(token).clear();
	}

	private String getInventionTitle(Integer docNumber) {
		return patents.get(docNumber).getInventionTitle();
	}

	@SuppressWarnings("unchecked")
	public boolean load() {
		try {
			tmp_index = new RandomAccessFile(TMP_POSTINGS_FILE, "rw");
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
		tmp_index.seek(tmp_index.length());
		int docNumber = posting.getDocNumber();
		tmp_index.writeInt(docNumber);
		tmp_index.writeShort(posting.getPosition());
	}

	private void postBlock(String token, List<YahoogleIndexPosting> postList) {
		Long offset = tmp_tokenOffsets.get(token);
		try {
			if (offset == null) {
				tmp_tokenOffsets.put(token, tmp_index.length());
			} else {
				tmp_index.seek(offset);
				Long next;
				while ((next = tmp_index.readLong()) != NO_NEXT_POSTING) {
					tmp_index.seek(next);
					offset = next;
				}
				tmp_index.seek(offset);
				tmp_index.writeLong(tmp_index.length());
			}
			tmp_index.seek(tmp_index.length());
			tmp_index.writeLong(NO_NEXT_POSTING);
			tmp_index.writeInt(postList.size());
			for (YahoogleIndexPosting post : postList) {
				post(post);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void queue(String token, YahoogleIndexPosting posting) {
		if (posts.get(token) == null) {
			posts.put(token, new ArrayList<YahoogleIndexPosting>());
		}
		posts.get(token).add(posting);
		flushCounter++;
		if (flushCounter > FLUSH_COUNTER_THRESHOLD) {
			if (Runtime.getRuntime().freeMemory() < FLUSH_MEM_THRESHOLD) {
				flush();
			}
		}
	}

	private void reorganize() {
		try {
			for (Entry<String, Long> entry : tmp_tokenOffsets.entrySet()) {
				long start = index.length();
				tokenOffsets.put(entry.getKey(), start);
				int total_size = 0;
				index.seek(start);
				index.writeInt(total_size);
				long offset = entry.getValue();
				while (offset != NO_NEXT_POSTING) {
					tmp_index.seek(offset);
					offset = tmp_index.readLong();
					int size = tmp_index.readInt();
					total_size += size;
					byte[] b = new byte[size * POST_SIZE];
					tmp_index.readFully(b);
					index.write(b);
				}
				index.seek(start);
				index.writeInt(total_size);
			}
			tmp_index.close();
			deleteIfExists(TMP_POSTINGS_FILE);
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
