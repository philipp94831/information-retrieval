package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.util.StringTokenizer;

import de.hpi.ir.yahoogle.Patent;
import de.hpi.ir.yahoogle.PatentParserCallback;
import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.StopWordList;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.index.Posting;

public class PartialIndex extends Loadable implements PatentParserCallback {

	private static final String BASE_NAME = ".index";
	private TokenDictionary dictionary;
	private String name;

	private PatentIndex patents;

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
		String text = patent.getPatentAbstract();
		StringTokenizer tokenizer = new StringTokenizer(text);
		int i = 1;
		while (tokenizer.hasMoreTokens()) {
			String token = Stemmer.stem(tokenizer.nextToken());
			if (StopWordList.isStopword(token)) {
				continue;
			}
			Posting posting = new Posting();
			posting.setPosition(i);
			dictionary.add(token, patent.getDocNumber(), posting);
			i++;
		}
		int wordCount = i - 1;
		resume.setWordCount(wordCount);
		patents.add(resume);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		patents = new PatentIndex(name);
		patents.create();
		dictionary = new TokenDictionary(name);
		dictionary.create();
	}

	public void delete() {
		patents.delete();
		dictionary.delete();
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	public TokenDictionary getDictionary() {
		return dictionary;
	}

	public PatentIndex getPatents() {
		return patents;
	}

	@Override
	public void load() throws IOException {
		patents = new PatentIndex(name);
		patents.load();
		dictionary = new TokenDictionary(name);
		dictionary.load();
	}

	@Override
	public void write() throws IOException {
		patents.write();
		dictionary.write();
	}

}
