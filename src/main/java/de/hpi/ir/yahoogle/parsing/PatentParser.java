package de.hpi.ir.yahoogle.parsing;

import java.io.InputStream;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class PatentParser {

	private StringBuffer buf = new StringBuffer();
	private PatentParserCallback callback;
	private Patent currentPatent;
	private String fileName;
	private boolean inAbstract = false;
	private boolean inDocNumber = false;
	private boolean inTitle = false;
	private Stack<String> parents;
	private XMLStreamReader2 xmlStreamReader;

	public PatentParser(PatentParserCallback smallIndex) {
		this.callback = smallIndex;
	}

	public void characters(String ch) {
		if (inAbstract || inTitle || inDocNumber) {
			buf.append(ch);
		}
	}

	public void endDocument() {
	}

	public void endElement(String qName) {
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
			try {
				currentPatent.setEnd(xmlStreamReader.getLocationInfo()
						.getEndingByteOffset());
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			callback.callback(currentPatent);
		}
	}

	public String getFileName() {
		return fileName;
	}

	private boolean isInAbstract(String qName) {
		return qName.equals("p") && parents.peek().equals("abstract");
	}

	private boolean isInDocNumber(String qName) {
		return qName.equals("doc-number") && parents
				.elementAt(parents.size() - 2).equals("publication-reference");
	}

	private boolean isInPatent(String qName) {
		return qName.equals("us-patent-grant");
	}

	private boolean isInTitle(String qName) {
		return qName.equals("invention-title");
	}

	private void parse() throws XMLStreamException {
		startDocument();
		while (xmlStreamReader.hasNext()) {
			int eventType = xmlStreamReader.next();
			switch (eventType) {
			case XMLEvent.START_DOCUMENT:
				startDocument();
				break;
			case XMLEvent.END_DOCUMENT:
				endDocument();
				break;
			case XMLEvent.START_ELEMENT:
				startElement(xmlStreamReader.getName().toString());
				break;
			case XMLEvent.CHARACTERS:
				characters(xmlStreamReader.getText());
				break;
			case XMLEvent.END_ELEMENT:
				endElement(xmlStreamReader.getName().toString());
				break;
			default:
				// do nothing
				break;
			}
		}
		endDocument();
	}

	public void parse(InputStream stream) throws XMLStreamException {
		XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) XMLInputFactory
				.newFactory();
		xmlStreamReader = (XMLStreamReader2) xmlInputFactory
				.createXMLStreamReader(stream);
		parse();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void startDocument() {
		parents = new Stack<String>();
	}

	public void startElement(String qName) {
		if (isInPatent(qName)) {
			currentPatent = new Patent(fileName);
			currentPatent.setStart(
					xmlStreamReader.getLocationInfo().getStartingByteOffset());
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
