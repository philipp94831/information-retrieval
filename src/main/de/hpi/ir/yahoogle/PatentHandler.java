package de.hpi.ir.yahoogle;

import java.io.ByteArrayInputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class PatentHandler extends DefaultHandler {
	
	private Stack<String> parents;
	
	@Override
	public void startDocument() {
		parents = new Stack<String>();
	}
	
	@Override
	public void endDocument() {
			
	}
	
	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		parents.push(qName);
	}
	
	@Override
	public void endElement(String uri, String name, String qName) {
		parents.pop();
	}
	
	private void primitivePrint(char ch[], int start, int length) {
		for(int i = start; i < start + length; i++) {
			switch(ch[i]) {
			case '\\':
				System.out.print("\\\\");
				break;
			case '"':
				System.out.print("\\\"");
				break;
			case '\n':
				System.out.print("\\n");
				break;
			case '\r':
				System.out.print("\\r");
				break;
			case '\t':
				System.out.print("\\t");
				break;
			default:
				System.out.print(ch[i]);
				break;
			}
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) {
		if (parents.peek().equals("invention-title")) {
			primitivePrint(ch, start, length);
			System.out.println();
		}
		if (parents.peek().equals("doc-number")) {
			Stack<String> tempStore = new Stack<String>();
			tempStore.push(parents.pop());
			tempStore.push(parents.pop());
			if (parents.peek().equals("publication-reference")) {
				primitivePrint(ch, start, length);
				System.out.print(": ");
			}
			parents.push(tempStore.pop());
			parents.push(tempStore.pop());
		}
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));	
	}
	
}
