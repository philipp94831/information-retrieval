package de.hpi.ir.yahoogle;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.hpi.ir.yahoogle.index.Index;

public class PatentParser extends DefaultHandler {

	private StringBuffer buf = new StringBuffer();
	private Patent currentPatent;
	private boolean inAbstract = false;
	private Index index;
	private boolean inDocNumber = false;
	private boolean inTitle = false;
	private Stack<String> parents;
	private String fileName;
	private FileInputStream input;

	public PatentParser(Index index) {
		super();
		this.index = index;
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (inAbstract || inTitle || inDocNumber) {
			buf.append(ch, start, length);
		}
	}

	@Override
	public void endDocument() {

	}

	@Override
	public void endElement(String uri, String name, String qName) {
		parents.pop();
		if (isInAbstract(qName)) {
			inAbstract = false;
			currentPatent.setPatentAbstract(buf.toString());
		}
		if (isInTitle(qName)) {
			inTitle = false;
			currentPatent.setInventionTitle(buf.toString());
		}
		if (isInDocNumber(qName)) {
			inDocNumber = false;
			currentPatent.setDocNumber(Integer.parseInt(buf.toString()));
		}
		if (isInPatent(qName)) {
			index.add(currentPatent);
		}
	}

	@Override
	public void error(SAXParseException ex) throws SAXException {
		System.out.println("ERROR: [at " + ex.getLineNumber() + "] " + ex);
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		System.out.println("FATAL_ERROR: [at " + ex.getLineNumber() + "] " + ex);
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		System.out.println("WARNING: [at " + ex.getLineNumber() + "] " + ex);
	}

	private boolean isInAbstract(String qName) {
		return qName.equals("p") && parents.peek().equals("abstract");
	}

	private boolean isInDocNumber(String qName) {
		return qName.equals("doc-number") && parents.elementAt(parents.size() - 2).equals("publication-reference");
	}

	private boolean isInPatent(String qName) {
		return qName.equals("us-patent-grant");
	}

	private boolean isInTitle(String qName) {
		return qName.equals("invention-title");
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
	}

	@Override
	public void startDocument() {
		parents = new Stack<String>();
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if (isInPatent(qName)) {
			currentPatent = new Patent(fileName);
		}
		if (isInAbstract(qName)) {
			inAbstract = true;
			buf = new StringBuffer();
		}
		if (isInDocNumber(qName)) {
			inDocNumber = true;
			buf = new StringBuffer();
		}
		if (isInTitle(qName)) {
			inTitle = true;
			buf = new StringBuffer();
		}
		parents.push(qName);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileInputStream getInput() {
		return input;
	}

	public void setInput(FileInputStream input) {
		this.input = input;
	}

}
