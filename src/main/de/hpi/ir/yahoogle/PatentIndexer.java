package de.hpi.ir.yahoogle;

import java.io.ByteArrayInputStream;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class PatentIndexer extends DefaultHandler {

	private StringBuffer buf = new StringBuffer();
	private Patent currentPatent;
	private boolean inAbstract = false;
	private YahoogleIndex index = new YahoogleIndex();
	private boolean inDocNumber = false;
	private boolean inTitle = false;
	private Stack<String> parents;

	public PatentIndexer() {
		super();
		index.create();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (inAbstract || inTitle || inDocNumber) {
			buf.append(ch, start, length);
		}
	}

	@Override
	public void endDocument() {
		index.finish();
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
			currentPatent.setDocNumber(buf.toString());
		}
		if (isInPatent(qName)) {
			index.add(currentPatent);
		}
	}

	public YahoogleIndex getIndex() {
		return index;
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
			currentPatent = new Patent();
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

}
