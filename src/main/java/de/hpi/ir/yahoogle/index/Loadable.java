package de.hpi.ir.yahoogle.index;

import java.io.File;
import java.io.IOException;

public abstract class Loadable {

	protected static final String FILE_EXTENSION = ".yahoogle";

	public static boolean deleteIfExists(String fileName) {
		File f = new File(fileName);
		return !f.exists() || f.delete();
	}

	public abstract void create() throws IOException;

	public abstract void load() throws IOException;

	public abstract void write() throws IOException;
}
