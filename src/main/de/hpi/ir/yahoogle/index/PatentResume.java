package de.hpi.ir.yahoogle.index;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.xml.stream.XMLStreamException;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.PatentParser;
import de.hpi.ir.yahoogle.PatentParserCallback;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class PatentResume implements PatentParserCallback {
	
	private int wordCount;
	private String fileName;
	private long start;
	private long end;
	private String patentFolder = "patents/";
	private Patent patent;
	
	public PatentResume(Patent patent) {
		this(patent.getFileName(), patent.getStart(), patent.getEnd());
	}

	public PatentResume(String fileName, long start, long end) {
		this.fileName = fileName;
		this.start = start;
		this.end = end;
	}

	public String getInventionTitle() {
		return getPatent().getInventionTitle();
	}

	private Patent getPatent() {
		if (patent == null) {
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
		return patent;
	}
	
	public byte[] toByteArray() throws IOException {
		ByteWriter out = new ByteWriter();
		out.writeUTF(fileName);
		out.writeLong(start);
		out.writeLong(end);
		return out.toByteArray();
	}
	
	public static PatentResume fromByteArray(byte[] bytes) {
		ByteReader in = new ByteReader(bytes);
		String fileName = in.readUTF();
		long start = in.readLong();
		long end = in.readLong();
		return new PatentResume(fileName, start, end);
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public String getPatentAbstract() {		
		return getPatent().getPatentAbstract();
	}

	@Override
	public void callback(Patent patent) {
		this.patent = patent;
	}

}
