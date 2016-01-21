package de.hpi.ir.yahoogle.parsing;

import java.io.InputStream;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class PatentParser {

	private StringBuilder buf = new StringBuilder();
	private final PatentParserCallback callback;
	private Patent currentPatent;
	private String fileName;
	private boolean inText = false;
	private Stack<String> parents;
	private XMLStreamReader2 xmlStreamReader;
	private Citation citation;

	public PatentParser(PatentParserCallback smallIndex) {
		this.callback = smallIndex;
	}

	private void characters(String ch) {
		if (inText) {
			buf.append(ch);
		}
	}

	private void endDocument() {
	}

	private void endElement(String qName) throws XMLStreamException {
		parents.pop();
		if (isInAbstract(qName)) {
			inText = false;
			currentPatent.setPatentAbstract(buf.toString());
		}
		if (isInDescription(qName)) {
			inText = false;
			currentPatent.addDescription(buf.toString());
		}
		if (isInTitle(qName)) {
			inText = false;
			currentPatent.setInventionTitle(buf.toString());
		}
		if (isInDocNumber(qName)) {
			inText = false;
			currentPatent.setDocNumber(Integer.parseInt(buf.toString()));
		}
		if (isInClaim(qName)) {
			inText = false;
			currentPatent.addClaim(buf.toString());
		}
		if(isInCitation(qName) && citation.isValid()) {
			currentPatent.addCitation(citation.getDocNumber());
		}
		if (isInCitationCountry(qName)) {
			inText = false;
			citation.setCountry(buf.toString());
		}
		if (isInCitationDocNumber(qName)) {
			inText = false;
			String docNumber = buf.toString();
			if(docNumber.matches("\\d+") && Long.parseLong(docNumber) <= Integer.MAX_VALUE) {
				citation.setDocNumber(Integer.parseInt(docNumber));
			}
		}
		if (isInCitationDate(qName)) {
			inText = false;
			citation.setDate(Integer.parseInt(buf.toString()));
		}
		if (isInPatent(qName)) {
			currentPatent.setEnd(
					xmlStreamReader.getLocationInfo().getEndingByteOffset());
			callback.receivePatent(currentPatent);
		}
	}

	private boolean isInAbstract(String qName) {
		return qName.equals("p") && parents.peek().equals("abstract");
	}

	private boolean isInClaim(String qName) {
		return qName.equals("claim") && parents.peek().equals("claims");
	}

	private boolean isInDescription(String qName) {
		return qName.equals("p") && parents.peek().equals("description");
	}

	private boolean isInCitation(String qName) {
		return qName.equals("document-id") && parents.peek().equals("patcit");
	}

	private boolean isInCitationCountry(String qName) {
		return qName.equals("country") && parents.peek().equals("document-id") && parents
				.elementAt(parents.size() - 2).equals("patcit");
	}

	private boolean isInCitationDocNumber(String qName) {
		return qName.equals("doc-number") && parents.peek().equals("document-id") && parents
				.elementAt(parents.size() - 2).equals("patcit");
	}

	private boolean isInCitationDate(String qName) {
		return qName.equals("date") && parents.peek().equals("document-id") && parents
				.elementAt(parents.size() - 2).equals("patcit");
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

	private void startDocument() {
		parents = new Stack<>();
	}

	private void startElement(String qName) {
		if (isInPatent(qName)) {
			currentPatent = new Patent(fileName);
			currentPatent.setStart(
					xmlStreamReader.getLocationInfo().getStartingByteOffset());
		}
		if (isInAbstract(qName) || isInDescription(qName)
				|| isInDocNumber(qName) || isInTitle(qName)
				|| isInClaim(qName) || isInCitationDate(qName) || isInCitationCountry(qName) || isInCitationDocNumber(qName)) {
			inText = true;
			buf = new StringBuilder();
		}
		if(isInCitation(qName)) {
			citation = new Citation();
		}
		parents.push(qName);
	}
}
