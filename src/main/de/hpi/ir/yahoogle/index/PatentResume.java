package de.hpi.ir.yahoogle.index;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.PatentParser;
import de.hpi.ir.yahoogle.PatentParserCallback;
import de.hpi.ir.yahoogle.PatentParts;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentResume implements Serializable, PatentParserCallback, Comparable<PatentResume> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1426829496447083131L;

	public static PatentResume fromByteArray(int docNumber, byte[] bytes) {
		return new PatentResume(docNumber, bytes);
	}

	private int docNumber;

	private long end;
	private String fileName;
	private TreeMap<Integer, PatentParts> parts = new TreeMap<Integer, PatentParts>();
	private transient Patent patent;
	private transient String patentFolder;
	private long start;

	private int wordCount;

	public PatentResume(int docNumber, byte[] bytes) {
		ByteReader in = new ByteReader(bytes);
		this.docNumber = docNumber;
		this.fileName = in.readUTF();
		this.start = in.readLong();
		this.end = in.readLong();
		this.wordCount = in.readInt();
		this.setTitlePosition(in.readInt());
		this.setAbstractPosition(in.readInt());

	}

	public PatentResume(Patent patent) {
		this.docNumber = patent.getDocNumber();
		this.fileName = patent.getFileName();
		this.start = patent.getStart();
		this.end = patent.getEnd();
	}

	@Override
	public void callback(Patent patent) {
		this.patent = patent;
	}

	@Override
	public int compareTo(PatentResume o) {
		return Integer.compare(docNumber, o.docNumber);
	}

	private void fetchPatent() {
		PatentParser parser = new PatentParser(this);
		try {
			RandomAccessFile file = new RandomAccessFile(patentFolder + "/" + fileName, "r");
			byte[] bytes = new byte[(int) (end - start)];
			file.seek(start);
			file.read(bytes);
			InputStream in = new ByteArrayInputStream(bytes);
			file.close();
			parser.parse(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getDocNumber() {
		return docNumber;
	}

	public PatentParts getPartAtPosition(int pos) {
		return parts.floorEntry(pos).getValue();
	}

	public Patent getPatent() {
		if (patent == null) {
			fetchPatent();
		}
		return patent;
	}

	public String getPatentFolder() {
		return patentFolder;
	}

	public int getPosition(PatentParts part) {
		for (Entry<Integer, PatentParts> entry : parts.entrySet()) {
			if (entry.getValue().equals(part)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setAbstractPosition(int pos) {
		parts.put(pos, PatentParts.ABSTRACT);
	}

	public void setPatentFolder(String patentFolder) {
		this.patentFolder = patentFolder;
	}

	public void setTitlePosition(int pos) {
		parts.put(pos, PatentParts.TITLE);
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public byte[] toByteArray() throws IOException {
		ByteWriter out = new ByteWriter();
		out.writeUTF(fileName);
		out.writeLong(start);
		out.writeLong(end);
		out.writeInt(wordCount);
		out.writeInt(getPosition(PatentParts.TITLE));
		out.writeInt(getPosition(PatentParts.ABSTRACT));
		return out.toByteArray();
	}

}
