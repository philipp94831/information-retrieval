package de.hpi.ir.yahoogle.index.generation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.Loadable;
import de.hpi.ir.yahoogle.index.Posting;
import de.hpi.ir.yahoogle.index.PostingList;
import de.hpi.ir.yahoogle.io.ObjectReader;
import de.hpi.ir.yahoogle.io.ObjectWriter;

public class TokenDictionary extends Loadable implements Iterable<PostingList> {

	private static final String BASE_NAME = ".dictionary";
	private Map<String, PostingList> dictionary;
	private String name;

	public TokenDictionary(String name) {
		this.name = name;
	}

	public void add(String token, int docNumber, Posting posting) {
		if (dictionary.get(token) == null) {
			dictionary.put(token, new PostingList(token));
		}
		dictionary.get(token).add(docNumber, posting);
	}

	@Override
	public void create() throws IOException {
		deleteIfExists(fileName());
		dictionary = new TreeMap<String, PostingList>();
	}

	public void delete() {
		deleteIfExists(fileName());
	}

	private String fileName() {
		return SearchEngineYahoogle.getTeamDirectory() + "/" + name + BASE_NAME + FILE_EXTENSION;
	}

	@Override
	public Iterator<PostingList> iterator() {
		return dictionary.values().iterator();
	}

	@Override
	public void load() throws IOException {
		dictionary = ObjectReader.<TreeMap<String, PostingList>> readObject(fileName());
	}

	@Override
	public void write() throws IOException {
		ObjectWriter.writeObject(dictionary, fileName());
	}

}
