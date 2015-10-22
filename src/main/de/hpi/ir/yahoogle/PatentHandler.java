package de.hpi.ir.yahoogle;

import java.io.ByteArrayInputStream;
import java.util.Stack;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class PatentHandler extends DefaultHandler {
	
	private Stack<String> parents;
	private boolean inAbstract = false;
	private StringBuffer buf = new StringBuffer();
	private Patent currentPatent;
	private YahoogleIndex index;
	
	@Override
	public void startDocument() {
		parents = new Stack<String>();
		index = new YahoogleIndex();
	}
	
	@Override
	public void endDocument() {
			
	}
	
	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if(qName.equals("us-patent-grant")) {
			currentPatent = new Patent();
		};
		if(qName.equals("p") && parents.peek().equals("abstract")) {
			inAbstract = true;
			buf = new StringBuffer();
		}
		parents.push(qName);
	}
	
	@Override
	public void endElement(String uri, String name, String qName) {
		parents.pop();
		if(qName.equals("p") && parents.peek().equals("abstract")) {
			inAbstract = false;
			currentPatent.setPatentAbstract(buf.toString());
		}
		if(qName.equals("us-patent-grant")) {
			indexPatent(currentPatent);
		};
	}
	
	private void indexPatent(Patent patent) {
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		for(int i = 0; tokenizer.hasMoreTokens(); i++) {
			YahoogleIndexPosting posting = new YahoogleIndexPosting();
			posting.setPosition(i);
			index.add(tokenizer.nextToken(), patent.getDocNumber(), posting);
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (inAbstract) {
			buf.append(ch, start, length);
		}
		if (parents.peek().equals("doc-number")) {
			Stack<String> tempStore = new Stack<String>();
			tempStore.push(parents.pop());
			tempStore.push(parents.pop());
			if (parents.peek().equals("publication-reference")) {
				buf = new StringBuffer();
				buf.append(ch, start, length);
				currentPatent.setDocNumber(buf.toString());
			}
			parents.push(tempStore.pop());
			parents.push(tempStore.pop());
		}
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));	
	}

	public YahoogleIndex getIndex() {
		return index;
	}
	
}
