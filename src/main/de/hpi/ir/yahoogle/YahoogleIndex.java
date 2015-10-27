package de.hpi.ir.yahoogle;

import java.io.ByteArrayOutputStream;
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

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleIndex {

	private static final int MEM_CHECK_THRESHOLD = 100; // check free memory every 100 tokens
	private static final long FLUSH_MEM_THRESHOLD = 20 * 1000 * 1000; // write to file at 20MB free memory
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
	private Map<String, List<YahoogleIndexPosting>> indexBuffer = new HashMap<String, List<YahoogleIndexPosting>>();
	private RandomAccessFile tmp_index, index;
	private Map<String, Long> tmp_tokenOffsets = new HashMap<String, Long>();
	private Map<String, Long> tokenOffsets = new HashMap<String, Long>();

	
	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * @param patent
	 */
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
			buffer(token, posting);
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

	/**
	 * @param token
	 * @return docnumbers of patents that contain the token
	 */
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

	/**
	 * flushes indexBuffer and reorganzie temporary index to final index
	 */
	public void finish() {
		flush();
		reorganize();
	}

	/**
	 * writes indexBuffer to temporary index on disk  
	 */
	private void flush() {
		for (Entry<String, List<YahoogleIndexPosting>> entry : indexBuffer.entrySet()) {
			writePostingBlock(entry.getKey(), entry.getValue());
		}
		indexBuffer.clear();
		flushCounter = 0;
	}

	private String getInventionTitle(Integer docNumber) {
		return patents.get(docNumber).getInventionTitle();
	}

	/**
	 * loads index from disk
	 * @return success value
	 */
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

	/**
	 * loads object from disk
	 * @param fileName the file where object is stored on disk
	 * @return deserialized object
	 */
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

	/**
	 * matches given docNumbers to invention titles
	 * @param docNumbers a set of docNumbers
	 * @return list of invention titles
	 */
	public ArrayList<String> matchInventionTitles(Set<Integer> docNumbers) {
		ArrayList<String> results = new ArrayList<String>();
		for (Integer docNumber : docNumbers) {
			results.add(getInventionTitle(docNumber));
		}
		return results;
	}

	private void writePostToStream(YahoogleIndexPosting posting, ByteArrayOutputStream bout) throws IOException {
		bout.write(ByteBuffer.allocate(Integer.BYTES).putInt(posting.getDocNumber()).array());
		bout.write(ByteBuffer.allocate(Short.BYTES).putShort(posting.getPosition()).array());
	}

	private void writePostingBlock(String token, List<YahoogleIndexPosting> postList) {
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
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream(postList.size() * POST_SIZE);
			
			for (YahoogleIndexPosting post : postList) {
				writePostToStream(post, bout);
			}
			tmp_index.write(bout.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void buffer(String token, YahoogleIndexPosting posting) {
		if (indexBuffer.get(token) == null) {
			indexBuffer.put(token, new ArrayList<YahoogleIndexPosting>());
		}
		indexBuffer.get(token).add(posting);
		flushCounter++;
		if (flushCounter > MEM_CHECK_THRESHOLD) {
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
