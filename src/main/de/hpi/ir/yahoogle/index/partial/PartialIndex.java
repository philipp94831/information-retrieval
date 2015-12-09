package de.hpi.ir.yahoogle.index.partial;

import java.io.IOException;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.Tokenizer;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.parsing.Patent;
import de.hpi.ir.yahoogle.parsing.PatentParserCallback;

public class PartialIndex extends Loadable implements PatentParserCallback {

	private static final String BASE_NAME = ".index";
	private PartialTokenDictionary dictionary;
	private String name;
	private PartialPatentIndex patents;

	public PartialIndex(String name) {
		this.name = name;
	}

	/**
	 * processes the patent and adds its tokens to the indexBuffer
	 * 
	 * @param patent
	 */
	@Override
	public void callback(Patent patent) {
		PatentResume resume = new PatentResume(patent);
		int wordCount = 0;
		int startOffset = 0;
		resume.setTitlePosition(startOffset + 1);
		String title = patent.getInventionTitle();
		Tokenizer tokenizer = new Tokenizer(title, true);
		while (tokenizer.hasNext()) {
			String token = Stemmer.stem(tokenizer.next());
			Posting posting = new Posting();
			posting.setPosition(startOffset + tokenizer.getPosition());
			dictionary.add(token, patent.getDocNumber(), posting);
		}
		wordCount += tokenizer.getPosition();
		startOffset += tokenizer.getPosition() + 1;
		resume.setAbstractPosition(startOffset + 1);
		String patentAbstract = patent.getPatentAbstract();
		tokenizer = new Tokenizer(patentAbstract, true);
		while (tokenizer.hasNext()) {
			String token = Stemmer.stem(tokenizer.next());
			Posting posting = new Posting();
			posting.setPosition(startOffset + tokenizer.getPosition());
			dictionary.add(token, patent.getDocNumber(), posting);
		}
		wordCount += tokenizer.getPosition();
		resume.setWordCount(wordCount);
		patents.add(resume);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		patents = new PartialPatentIndex(name);
		patents.create();
		dictionary = new PartialTokenDictionary(name);
		dictionary.create();
	}

	public void delete() {
		try {
			patents.delete();
			dictionary.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	public PartialTokenDictionary getDictionary() {
		return dictionary;
	}

	public PartialPatentIndex getPatents() {
		return patents;
	}

	@Override
	public void load() throws IOException {
		patents = new PartialPatentIndex(name);
		patents.load();
		dictionary = new PartialTokenDictionary(name);
		dictionary.load();
	}

	@Override
	public void write() throws IOException {
		patents.write();
		dictionary.write();
	}
}
