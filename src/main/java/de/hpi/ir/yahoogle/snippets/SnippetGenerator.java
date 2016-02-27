package de.hpi.ir.yahoogle.snippets;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.language.Tokenizer;
import de.hpi.ir.yahoogle.parsing.PatentPart;
import de.hpi.ir.yahoogle.retrieval.Result;

public class SnippetGenerator {

	static final int MAX_WINDOW_LENGTH = 15;
	private final Index index;

	public SnippetGenerator(Index index) {
		this.index = index;
	}

	private SnippetWindow buildWindow(Result result, int i, List<String> phrases) {
		SnippetWindow window = new SnippetWindow(i);
		for (String phrase : phrases) {
			TreeSet<Integer> positions = new TreeSet<>(
					result.getPositions(phrase));
			int tokensInPhrase = new Tokenizer(phrase).countTokens();
			NavigableSet<Integer> matches = positions.tailSet(i, true)
					.headSet(i + MAX_WINDOW_LENGTH - tokensInPhrase, true);
			if (matches.size() > 0) {
				window.addDistinctMatch();
				int left = matches.first() - i;
				int right = matches.last() - i + tokensInPhrase - 1;
				window.checkLeftMost(left);
				window.checkRightMost(right);
			}
			for (int j = 0; j < tokensInPhrase; j++) {
				int _j = j;
				window.addMatches(matches.stream().map(k -> k + _j)
						.collect(Collectors.toList()));
			}
		}
		return window;
	}

	private String generate(Result result, PatentResume resume, List<String> phrases) {
		String patentAbstract = resume.getPatentAbstract();
		if (patentAbstract == null) {
			return "[Snippet not available]";
		}
		Tokenizer tokenizer = new Tokenizer(patentAbstract);
		while (tokenizer.hasNext()) {
			tokenizer.next();
		}
		int numberOfTokens = tokenizer.getPosition() - 1;
		int start = resume.getPosition(PatentPart.ABSTRACT);
		SnippetWindow bestWindow = new SnippetWindow(start);
		for (int i = start; i <= Math.max(1,
				start + numberOfTokens - MAX_WINDOW_LENGTH); i++) {
			SnippetWindow window = buildWindow(result, i, phrases);
			if (bestWindow.compareTo(window) > 0) {
				bestWindow = window;
			}
		}
		tokenizer = new Tokenizer(patentAbstract, false, true);
		StringBuilder snippet = new StringBuilder();
		if (bestWindow.getPosition() != start) {
			snippet.append("...");
		}
		while (tokenizer.hasNext()
				&& start + tokenizer.getPosition() < bestWindow.getPosition()
						+ MAX_WINDOW_LENGTH) {
			int position = start + tokenizer.getPosition();
			if (position >= bestWindow.getPosition()) {
				String token = tokenizer.next();
				if (tokenizer.isRealToken()
						&& bestWindow.getMatches().contains(position)) {
					// token = "{" + token + "}";
				}
				snippet.append(token);
			} else {
				tokenizer.next();
			}
		}
		if (tokenizer.hasNext()) {
			snippet.append(" ...");
		}
		return snippet.toString().trim();
	}

	public Map<Integer, String> generateSnippets(Collection<? extends Result> results, List<String> phrases) {
		Map<Integer, String> snippets = new HashMap<>();
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			String snippet = generate(result, index.getPatent(docNumber),
					phrases);
			snippets.put(docNumber, snippet);
		}
		return snippets;
	}
}
