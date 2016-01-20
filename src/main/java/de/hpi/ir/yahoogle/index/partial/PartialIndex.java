package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.Tokenizer;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.parsing.Patent;
import de.hpi.ir.yahoogle.parsing.PatentParserCallback;
import de.hpi.ir.yahoogle.parsing.PatentPart;

public class PartialIndex extends Loadable implements PatentParserCallback {

	private static final String BASE_NAME = ".index";
	private static final Logger LOGGER = Logger
			.getLogger(PartialIndex.class.getName());
	private static final boolean SKIP_STOPWORDS = true;
	private PartialTokenDictionary dictionary;
	private final String name;
	private PartialPatentIndex patents;
	private PartialCitationIndex citations;
	private PatentResume resume;
	private int startOffset;
	private int wordCount;

	public PartialIndex(String name) {
		this.name = name;
	}

	@Override
	public void callback(Patent patent) {
		resume = new PatentResume(patent);
		wordCount = 0;
		startOffset = 0;
		indexTitle(patent.getInventionTitle());
		indexAbstract(patent.getPatentAbstract());
		indexDescriptions(patent.getDescriptions());
		indexClaims(patent.getClaims());
		indexCitations(patent.getCitations());
		resume.setWordCount(wordCount);
		patents.add(resume);
	}

	private void indexCitations(List<Integer> cited) {
		for(Integer citation : cited) {
			citations.add(citation, resume.getDocNumber());
		}
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		patents = new PartialPatentIndex(name);
		patents.create();
		citations = new PartialCitationIndex(name);
		citations.create();
		dictionary = new PartialTokenDictionary(name);
		dictionary.create();
	}

	public void delete() {
		try {
			patents.delete();
			dictionary.delete();
			citations.delete();
		} catch (IOException e) {
			LOGGER.warning("Could not delete partial index " + name);
		}
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME
				+ FILE_EXTENSION;
	}

	public PartialTokenDictionary getDictionary() {
		return dictionary;
	}

	public PartialPatentIndex getPatents() {
		return patents;
	}

	public PartialCitationIndex getCitations() {
		return citations;
	}

	private void indexAbstract(String patentAbstract) {
		resume.setPosition(PatentPart.ABSTRACT, startOffset + 1);
		Tokenizer tokenizer = new Tokenizer(patentAbstract, SKIP_STOPWORDS);
		while (tokenizer.hasNext()) {
			String token = Stemmer.stem(tokenizer.next());
			Posting posting = new Posting();
			posting.setPosition(startOffset + tokenizer.getPosition());
			dictionary.add(token, resume.getDocNumber(), posting);
		}
		wordCount += tokenizer.getPosition();
		startOffset += tokenizer.getPosition() + 1;
	}

	private void indexClaims(List<String> claims) {
		resume.setPosition(PatentPart.CLAIM, startOffset + 1);
		for (String claim : claims) {
			Tokenizer tokenizer = new Tokenizer(claim, SKIP_STOPWORDS);
			while (tokenizer.hasNext()) {
				String token = Stemmer.stem(tokenizer.next());
				Posting posting = new Posting();
				posting.setPosition(startOffset + tokenizer.getPosition());
				dictionary.add(token, resume.getDocNumber(), posting);
			}
			wordCount += tokenizer.getPosition();
			startOffset += tokenizer.getPosition() + 1;
		}
	}

	private void indexDescriptions(List<String> descriptions) {
		resume.setPosition(PatentPart.DESCRIPTION, startOffset + 1);
		for (String description : descriptions) {
			Tokenizer tokenizer = new Tokenizer(description, SKIP_STOPWORDS);
			while (tokenizer.hasNext()) {
				String token = Stemmer.stem(tokenizer.next());
				Posting posting = new Posting();
				posting.setPosition(startOffset + tokenizer.getPosition());
				dictionary.add(token, resume.getDocNumber(), posting);
			}
			wordCount += tokenizer.getPosition();
			startOffset += tokenizer.getPosition() + 1;
		}
	}

	private void indexTitle(String title) {
		resume.setPosition(PatentPart.TITLE, startOffset + 1);
		Tokenizer tokenizer = new Tokenizer(title, SKIP_STOPWORDS);
		while (tokenizer.hasNext()) {
			String token = Stemmer.stem(tokenizer.next());
			Posting posting = new Posting();
			posting.setPosition(startOffset + tokenizer.getPosition());
			dictionary.add(token, resume.getDocNumber(), posting);
		}
		wordCount += tokenizer.getPosition();
		startOffset += tokenizer.getPosition() + 1;
	}

	@Override
	public void load() throws IOException {
		patents = new PartialPatentIndex(name);
		patents.load();
		citations = new PartialCitationIndex(name);
		citations.load();
		dictionary = new PartialTokenDictionary(name);
		dictionary.load();
	}

	@Override
	public void write() throws IOException {
		patents.write();
		dictionary.write();
		citations.write();
	}
}
