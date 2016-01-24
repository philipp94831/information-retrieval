package de.hpi.ir.yahoogle.snippets;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.Tokenizer;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.parsing.PatentPart;
import de.hpi.ir.yahoogle.rm.Result;

public class SnippetGenerator {

	static final int MAX_WINDOW_LENGTH = 10;
	private final List<String> phrases;

	public SnippetGenerator(List<String> phrases) {
		this.phrases = phrases;
	}

	private SnippetWindow buildWindow(Result result, int i) {
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

	public String generate(Result result, PatentResume resume) {
		String patentAbstract = resume.getPatent().getPatentAbstract();
		Tokenizer tokenizer = new Tokenizer(patentAbstract);
		while (tokenizer.hasNext()) {
			tokenizer.next();
		}
		int numberOfTokens = tokenizer.getPosition() - 1;
		int start = resume.getPosition(PatentPart.ABSTRACT);
		SnippetWindow bestWindow = new SnippetWindow(start);
		for (int i = start; i <= Math.max(1,
				start + numberOfTokens - MAX_WINDOW_LENGTH); i++) {
			SnippetWindow window = buildWindow(result, i);
			if (bestWindow.compareTo(window) > 0) {
				bestWindow = window;
			}
		}
		tokenizer = new Tokenizer(patentAbstract, false, true);
		StringBuilder snippet = new StringBuilder();
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
		return snippet.toString().trim();
	}
}
