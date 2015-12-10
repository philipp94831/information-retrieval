package de.hpi.ir.yahoogle.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectReader {

	public static <T> T readObject(String fileName) {
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
