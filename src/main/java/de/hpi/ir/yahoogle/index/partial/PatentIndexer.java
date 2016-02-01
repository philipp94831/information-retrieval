package de.hpi.ir.yahoogle.index.partial;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.parsing.PatentParser;

public class PatentIndexer extends Thread {

	private static final Logger LOGGER = Logger
			.getLogger(PatentIndexer.class.getName());
	private Queue<File> files;
	private PatentReceiver receiver;

	public PatentIndexer(String name, Queue<File> files) {
		this.files = files;
		receiver = new PatentReceiver(name);
	}

	@Override
	public void run() {
		try {
			File patentFile;
			receiver.start();
			PatentParser handler = new PatentParser(receiver);
			while ((patentFile = files.poll()) != null) {
				LOGGER.info(patentFile.getName());
				FileInputStream stream = new FileInputStream(patentFile);
				handler.setFileName(patentFile.getName());
				handler.parse(stream);
			}
			receiver.finish();
		} catch (IOException e) {
			LOGGER.severe("Error indexing files");
		} catch (XMLStreamException e) {
			LOGGER.severe("Error parsing XML");
		}
	}

	public List<String> getNames() {
		return receiver.getNames();
	}
}
