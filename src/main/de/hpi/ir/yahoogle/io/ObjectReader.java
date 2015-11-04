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
	@SuppressWarnings("unchecked")
	public static <T> T readObject(T o, String fileName) {
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream oin = new ObjectInputStream(fin);
			o = (T) oin.readObject();
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

}
