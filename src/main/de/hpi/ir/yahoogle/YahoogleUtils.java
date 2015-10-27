package de.hpi.ir.yahoogle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.lemurproject.kstem.KrovetzStemmer;

public class YahoogleUtils {

	private static final String STOPWORDS_FILE = "res/stopwords.txt";
	private static StopWordList stopwords = new StopWordList(STOPWORDS_FILE);

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

	public static boolean isStopword(String word) {
		return stopwords.contains(word);
	}

	/**
	 * loads object from disk
	 * 
	 * @param fileName
	 *            the file where object is stored on disk
	 * @return deserialized object
	 */
	public static Object loadObject(String fileName) {
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

	public static String sanitize(String word) {
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(word.toLowerCase().replaceAll("\\W", ""));
	}

	public static boolean writeObject(Object o, String fileName) {
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
