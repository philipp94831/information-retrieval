package de.hpi.ir.yahoogle;

import java.util.List;
import java.util.NavigableSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.index.PatentResume;

public class SnippetGenerator {

	protected static final int MAX_WINDOW_LENGTH = 10;
	private List<String> phrases;

	public SnippetGenerator(List<String> phrases) {
		this.phrases = phrases;
	}

	public String generate(Result result, PatentResume resume) {
		String patentAbstract = resume.getPatent().getPatentAbstract();
		Tokenizer tokenizer = new Tokenizer(patentAbstract, true);
		while (tokenizer.hasNext()) {
			tokenizer.next();
		}
		int numberOfTokens = tokenizer.getPosition() - 1;
		int start = resume.getPosition(PatentParts.ABSTRACT);
		SnippetWindow bestWindow = new SnippetWindow(start);
		for (int i = start; i <= Math.max(1, start + numberOfTokens - MAX_WINDOW_LENGTH); i++) {
			SnippetWindow window = new SnippetWindow(i);
			for (String phrase : phrases) {
				TreeSet<Integer> positions = new TreeSet<Integer>(result.getPositions(phrase));
				int tokensInPhrase = new StringTokenizer(phrase).countTokens();
				NavigableSet<Integer> matches = positions.tailSet(i, true)
						.headSet(i + MAX_WINDOW_LENGTH - tokensInPhrase, true);
				if (matches.size() > 0) {
					window.addDistinctMatch();
					int left = matches.first() - i;
					int right = matches.last() - i + tokensInPhrase - 1;
					window.checkLeftMost(left);
					window.checkRightMost(right);
				}
				window.addMatches(matches.size());
			}
			if (bestWindow.compareTo(window) < 0) {
				bestWindow = window;
			}
		}
		StringTokenizer tokenizer2 = new StringTokenizer(patentAbstract);
		int currentPosition = start;
		StringBuilder snippet = new StringBuilder();
		while (tokenizer2.hasMoreTokens() && currentPosition < bestWindow.getPosition() + MAX_WINDOW_LENGTH) {
			String token = tokenizer2.nextToken();
			if (currentPosition >= bestWindow.getPosition()) {
				snippet.append(" " + token);
			}
			if (StopWordList.isStopword(token)) {
				continue;
			}
			currentPosition++;
		}
		return snippet.toString().trim();
	}

}
