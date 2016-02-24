package de.hpi.ir.yahoogle.util;

import java.io.File;

public class FileUtils {

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}
}
