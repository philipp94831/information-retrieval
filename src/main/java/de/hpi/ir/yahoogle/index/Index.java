package de.hpi.ir.yahoogle.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.index.partial.PartialIndex;
import de.hpi.ir.yahoogle.index.search.PhraseResultIterator;
import de.hpi.ir.yahoogle.index.search.TokenResultIterator;
import de.hpi.ir.yahoogle.language.Stemmer;
import de.hpi.ir.yahoogle.language.Tokenizer;

public class Index extends Loadable {

	private static final String DICTIONARY_FILE = SearchEngineYahoogle.getTeamDirectory() + "/dictionary.txt";
	private static final Logger LOGGER = Logger.getLogger(Index.class.getName());
	private static final boolean PRINT_DICTIONARY = false;
	private CitationIndex citations;
	private TokenDictionary dictionary;
	private PatentIndex patents;
	private final String patentsFolder;

	public Index(String patentsFolder) {
		this.patentsFolder = patentsFolder;
	}

	@Override
	public void create() throws IOException {
		patents = new PatentIndex(patentsFolder);
		patents.create();
		dictionary = new TokenDictionary();
		dictionary.create();
		citations = new CitationIndex();
		citations.create();
	}

	public Set<Integer> find(List<String> phrases) {
		return phrases.stream().map(this::find).flatMap(Set::stream).collect(Collectors.toSet());
	}

	private Set<Integer> find(String phrase) {
		Set<Integer> result = new HashSet<>();
		PhraseResultIterator iterator = findPositions(phrase);
		while(iterator.hasNext()) {
			result.add(iterator.next().getDocNumber());
		}
		return result;
	}

	private TokenResultIterator findAll(String token) {
		List<String> tokens = new ArrayList<>();
		if (isPrefixToken(token)) {
			String prefix = token.substring(0, token.length() - 1);
			tokens = dictionary.getTokensForPrefix(prefix);
		} else {
			tokens.add(Stemmer.stem(token));
		}
		return new TokenResultIterator(tokens.stream().map(dictionary::find).collect(Collectors.toList()));
	}

	private boolean isPrefixToken(String token) {
		return token.endsWith("*");
	}

	private List<Integer> findLinks(String phrase) {
		return citations.find(Integer.parseInt(phrase.trim()));
	}

	public Set<Integer> findLinks(List<String> phrases) {
		return phrases.stream().map(this::findLinks).flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public PhraseResultIterator findPositions(String phrase) {
		Tokenizer tokenizer = new Tokenizer(phrase);
		List<String> tokens = new ArrayList<>();
		while (tokenizer.hasNext()) {
			tokens.add(tokenizer.next());
		}
		return new PhraseResultIterator(tokens.stream().map(this::findAll).collect(Collectors.toList()));
	}

	public Set<Integer> getAllDocNumbers() {
		return patents.getAllDocNumbers();
	}

	public PatentResume getPatent(int docNumber) {
		return patents.get(docNumber);
	}

	@Override
	public void load() throws IOException {
		patents = new PatentIndex(patentsFolder);
		patents.load();
		dictionary = new TokenDictionary();
		dictionary.load();
		citations = new CitationIndex();
		citations.load();
		if (PRINT_DICTIONARY) {
			printDictionary();
		}
	}

	public void mergeIndices(List<String> names) throws IOException {
		List<PartialIndex> indexes = new ArrayList<>();
		for (String name : names) {
			PartialIndex index = new PartialIndex(name);
			index.load();
			indexes.add(index);
		}
		patents.merge(indexes.stream().map(PartialIndex::getPatents).collect(Collectors.toList()));
		dictionary.merge(indexes.stream().map(PartialIndex::getDictionary).collect(Collectors.toList()));
		citations.merge(indexes.stream().map(PartialIndex::getCitations).collect(Collectors.toList()));
		indexes.forEach(PartialIndex::delete);
		LOGGER.info("finished merging");
	}

	public void calculatePageRank() throws IOException {
		LOGGER.info("calculating PageRank");
		Set<Integer> all = patents.getAllDocNumbers();
		Map<Integer, List<Integer>> cites = new HashMap<>();
		all.forEach(d -> cites.put(d, citations.find(d)));
		PageRank pr = new PageRank(cites);
		Map<Integer, Double> pageRank = pr.compute();
		for (int docNumber : all) {
			PatentResume resume = patents.get(docNumber);
			resume.setPageRank(pageRank.get(docNumber));
			patents.update(resume);
		}
		LOGGER.info("finished calculating PageRank");
	}

	private void printDictionary() {
		try {
			deleteIfExists(DICTIONARY_FILE);
			PrintWriter writer = new PrintWriter(DICTIONARY_FILE, "UTF-8");
			dictionary.getTokens().forEach(writer::println);
			writer.close();
		} catch (FileNotFoundException e) {
			LOGGER.warning("File for printing dictionary not found");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Unsupported encoding when printing dictionary");
		}
	}

	public void warmUp() {
		patents.warmUp();
		citations.warmUp();
		dictionary.warmUp();
	}

	public int wordCount() {
		return patents.getTotalWordCount();
	}

	@Override
	public void write() throws IOException {
		patents.write();
		dictionary.write();
		citations.write();
	}
}
