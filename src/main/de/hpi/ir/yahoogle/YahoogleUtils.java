package de.hpi.ir.yahoogle;

import java.io.File;

public class YahoogleUtils {

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

}
