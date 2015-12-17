package de.hpi.ir.yahoogle.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.Tokenizer;
import de.hpi.ir.yahoogle.index.partial.PartialIndex;
import de.hpi.ir.yahoogle.rm.Model;
import de.hpi.ir.yahoogle.rm.ModelResult;
import de.hpi.ir.yahoogle.rm.ModelResultComparator;
import de.hpi.ir.yahoogle.rm.QLModel;
import de.hpi.ir.yahoogle.rm.Result;

public class Index extends Loadable {

	private static final String DICTIONARY_FILE = SearchEngineYahoogle
			.getTeamDirectory() + "/dictionary.txt";
	private static final Logger LOGGER = Logger
			.getLogger(Index.class.getName());
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
	}

	public List<Result> find(String phrase) {
		Map<Integer, Set<Integer>> result = findWithPositions(phrase);
		return result.entrySet().stream().filter(e -> e.getValue().size() > 0)
				.map(e -> {
					Result r = new Result(e.getKey());
					r.addPositions(phrase, e.getValue());
					return r;
				}).collect(Collectors.toList());
	}

	private Map<Integer, Set<Integer>> findAll(String token) {
		boolean prefix = token.endsWith("*");
		if (prefix) {
			String pre = token.substring(0, token.length() - 1);
			Map<Integer, Set<Integer>> result = new HashMap<>();
			for (String t : dictionary.getTokensForPrefix(pre)) {
				dictionary.find(t).entrySet().forEach(e -> result
						.merge(e.getKey(), e.getValue(), (v1, v2) -> {
							v1.addAll(v2);
							return v1;
						}));
			}
			return result;
		} else {
			return dictionary.find(Stemmer.stem(token));
		}
	}

	public List<ModelResult> findRelevant(List<String> phrases, int topK) {
		Model model = new QLModel(this);
		List<ModelResult> results = model.compute(phrases);
		Collections.sort(results, new ModelResultComparator());
		return results.subList(0, Math.min(topK, results.size()));
	}

	public Map<Integer, Set<Integer>> findWithPositions(String phrase) {
		Tokenizer tokenizer = new Tokenizer(phrase);
		Map<Integer, Set<Integer>> result = null;
		if (tokenizer.hasNext()) {
			result = findAll(tokenizer.next());
		}
		for (int i = 1; tokenizer.hasNext(); i++) {
			Map<Integer, Set<Integer>> newResult = findAll(tokenizer.next());
			matchNextPhraseToken(result, newResult, i);
		}
		return result;
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
		// printDictionary();
	}

	private void matchNextPhraseToken(Map<Integer, Set<Integer>> result, Map<Integer, Set<Integer>> nextResult, int delta) {
		for (Entry<Integer, Set<Integer>> entry : result.entrySet()) {
			Set<Integer> newPos = nextResult.get(entry.getKey());
			if (newPos != null) {
				Set<Integer> oldPos = entry.getValue();
				oldPos.retainAll(newPos.stream().map(p -> p - delta)
						.collect(Collectors.toSet()));
				result.put(entry.getKey(), oldPos);
			} else {
				result.get(entry.getKey()).clear();
			}
		}
	}

	public void mergeIndices(List<String> names) throws IOException {
		List<PartialIndex> indexes = new ArrayList<>();
		for (String name : names) {
			PartialIndex index = new PartialIndex(name);
			index.load();
			indexes.add(index);
		}
		patents.merge(indexes.stream().map(PartialIndex::getPatents)
				.collect(Collectors.toList()));
		dictionary.merge(indexes.stream().map(PartialIndex::getDictionary)
				.collect(Collectors.toList()));
		indexes.forEach(PartialIndex::delete);
	}

	@SuppressWarnings("unused")
	private void printDictionary() {
		try {
			new File(DICTIONARY_FILE).delete();
			PrintWriter writer = new PrintWriter(DICTIONARY_FILE, "UTF-8");
			dictionary.getTokens().forEach(writer::println);
			writer.close();
		} catch (FileNotFoundException e) {
			LOGGER.warning("File for printing dictionary not found");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Unsupported encoding when printing dictionary");
		}
	}

	public int wordCount() {
		return patents.getTotalWordCount();
	}

	@Override
	public void write() throws IOException {
		patents.write();
		dictionary.write();
	}
}
