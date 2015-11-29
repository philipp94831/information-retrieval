package de.hpi.ir.yahoogle.index;

import java.util.List;
import java.util.NavigableSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import de.hpi.ir.yahoogle.Stemmer;
import de.hpi.ir.yahoogle.StopWordList;
import de.hpi.ir.yahoogle.rm.ModelResult;

public class SnippetGenerator {

	private static final int MAX_WINDOW_LENGTH = 10;
	private List<String> phrases;

	public SnippetGenerator(List<String> phrases) {
		this.phrases = phrases;
	}

	public String generate(ModelResult result, String patentAbstract) {
		StringTokenizer tokenizer = new StringTokenizer(patentAbstract);
		int numberOfTokens = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = Stemmer.stem(tokenizer.nextToken());
			if (StopWordList.isStopword(token)) {
				continue;
			}
			numberOfTokens++;
		}
		int bestWindow = 0;
		int distinctMatchesInBestWindow = 0;
		int matchesInBestWindow = 0;
		int middleAlignInBestWindow = 0;
		for (int i = 1; i <= Math.max(1, numberOfTokens - MAX_WINDOW_LENGTH + 1); i++) {
			int distinctMatches = 0;
			int matchesInWindow = 0;
			int leftMostPosition = numberOfTokens + 1;
			int rightMostPosition = 0;
			for (String phrase : phrases) {
				TreeSet<Integer> positions = new TreeSet<Integer>(result.getPositions(phrase));
				int tokensInPhrase = new StringTokenizer(phrase).countTokens();
				NavigableSet<Integer> matches = positions
						.tailSet(i, true)
						.headSet(i + MAX_WINDOW_LENGTH - tokensInPhrase, true);
				if (matches.size() > 0) {
					distinctMatches++;
				}
				matchesInWindow += matches.size();
				if (matches.size() > 0) {
					int left = matches.first();
					int right = matches.last() + tokensInPhrase - 1;
					leftMostPosition =  left < leftMostPosition ? left : leftMostPosition;
					rightMostPosition = right > rightMostPosition ? right : rightMostPosition;
				}
			}
			int middleAlign = calculateMiddleAlign(i, leftMostPosition, rightMostPosition);
			if (distinctMatchesInBestWindow < distinctMatches
					|| distinctMatchesInBestWindow == distinctMatches && (matchesInBestWindow < matchesInWindow
							|| matchesInBestWindow <= matchesInWindow && middleAlign < middleAlignInBestWindow)
					|| distinctMatchesInBestWindow <= distinctMatches && middleAlign < middleAlignInBestWindow) {
				bestWindow = i;
				distinctMatchesInBestWindow = distinctMatches;
				matchesInBestWindow = matchesInWindow;
				middleAlignInBestWindow = middleAlign;
			}
		}
		tokenizer = new StringTokenizer(patentAbstract);
		int currentPosition = 1;
		StringBuilder snippet = new StringBuilder();
		while (tokenizer.hasMoreTokens() && currentPosition < bestWindow + MAX_WINDOW_LENGTH) {
			String token = tokenizer.nextToken();
			if (currentPosition >= bestWindow) {
				snippet.append(" " + token);
			}
			if (StopWordList.isStopword(token)) {
				continue;
			}
			currentPosition++;
		}
		return snippet.toString().trim();
	}

	private int calculateMiddleAlign(int i, int leftMostPosition, int rightMostPosition) {
		int leftDelta = leftMostPosition - i;
		int rightDelta = i + MAX_WINDOW_LENGTH - 1 - rightMostPosition;
		int middleAlign = leftDelta * leftDelta + rightDelta * rightDelta;
		return middleAlign;
	}

}
