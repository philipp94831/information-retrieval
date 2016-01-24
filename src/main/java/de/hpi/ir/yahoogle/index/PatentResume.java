package de.hpi.ir.yahoogle.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.parsing.Patent;
import de.hpi.ir.yahoogle.parsing.PatentParser;
import de.hpi.ir.yahoogle.parsing.PatentParserCallback;
import de.hpi.ir.yahoogle.parsing.PatentPart;

public class PatentResume
		implements PatentParserCallback, Comparable<PatentResume> {

	private static final Logger LOGGER = Logger
			.getLogger(PatentResume.class.getName());
	private final int docNumber;
	private final long end;
	private final String fileName;
	private final TreeMap<Integer, PatentPart> parts = new TreeMap<>();
	private Patent patent;
	private String patentFolder;
	private final long start;
	private int wordCount;

	public PatentResume(byte[] bytes) {
		ByteReader in = new ByteReader(bytes);
		this.docNumber = in.readInt();
		this.fileName = in.readUTF();
		this.start = in.readLong();
		this.end = in.readLong();
		this.wordCount = in.readInt();
		for (PatentPart part : PatentPart.values()) {
			this.setPosition(part, in.readInt());
		}
	}

	public PatentResume(Patent patent) {
		this.docNumber = patent.getDocNumber();
		this.fileName = patent.getFileName();
		this.start = patent.getStart();
		this.end = patent.getEnd();
	}

	@Override
	public int compareTo(PatentResume o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	private void fetchPatent() {
		PatentParser parser = new PatentParser(this);
		try {
			RandomAccessFile file = new RandomAccessFile(getFullFileName(),
					"r");
			byte[] bytes = new byte[(int) (end - start)];
			file.seek(start);
			file.read(bytes);
			file.close();
			InputStream in = new ByteArrayInputStream(bytes);
			parser.parse(in);
		} catch (IOException e) {
			LOGGER.severe("Error accessing patent " + docNumber + " in file "
					+ fileName);
		} catch (XMLStreamException e) {
			LOGGER.severe("Error parsing XML for patent " + docNumber
					+ " in file " + fileName);
		}
	}

	public int getDocNumber() {
		return docNumber;
	}

	private String getFullFileName() {
		return patentFolder + "/" + fileName;
	}

	public PatentPart getPartAtPosition(int pos) {
		return parts.floorEntry(pos).getValue();
	}

	public Patent getPatent() {
		if (patent == null) {
			fetchPatent();
		}
		return patent;
	}

	public int getPosition(PatentPart part) {
		for (Entry<Integer, PatentPart> entry : parts.entrySet()) {
			if (entry.getValue().equals(part)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public int getWordCount() {
		return wordCount;
	}

	@Override
	public void receivePatent(Patent patent) {
		this.patent = patent;
	}

	public void setPatentFolder(String patentFolder) {
		this.patentFolder = patentFolder;
	}

	public void setPosition(PatentPart part, int pos) {
		parts.put(pos, part);
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public byte[] toByteArray() throws IOException {
		ByteWriter out = new ByteWriter();
		out.writeInt(docNumber);
		out.writeUTF(fileName);
		out.writeLong(start);
		out.writeLong(end);
		out.writeInt(wordCount);
		for (PatentPart part : PatentPart.values()) {
			out.writeInt(getPosition(part));
		}
		return out.toByteArray();
	}
}
