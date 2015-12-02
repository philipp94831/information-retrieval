package de.hpi.ir.yahoogle.index;

import java.io.IOException;

public interface Loadable {

	void write() throws IOException;

	void create() throws IOException;

	void load() throws IOException;

}
