package de.hpi.ir.yahoogle.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectReader {

	/**
	 * loads object from disk
	 * 
	 * @param fileName
	 *            the file where object is stored on disk
	 * @return deserialized object
	 */
	public static <T> T readObject(String fileName) throws FileNotFoundException {
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream oin = new ObjectInputStream(fin);
			@SuppressWarnings("unchecked")
			T o = (T) oin.readObject();
			oin.close();
			fin.close();
			return o;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
