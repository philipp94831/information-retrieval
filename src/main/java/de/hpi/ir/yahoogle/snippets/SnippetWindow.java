package de.hpi.ir.yahoogle.snippets;

import java.util.ArrayList;
import java.util.List;

public class SnippetWindow implements Comparable<SnippetWindow> {

	private int distinctMatches = 0;
	private int leftMostPosition = SnippetGenerator.MAX_WINDOW_LENGTH;
	private final List<Integer> matches = new ArrayList<>();
	private int numberOfMatches = 0;
	private final int pos;
	private int rightMostPosition = 0;

	public SnippetWindow(int pos) {
		this.pos = pos;
	}

	public void addDistinctMatch() {
		distinctMatches++;
	}

	public void addMatches(List<Integer> matches) {
		this.matches.addAll(matches);
		this.numberOfMatches += matches.size();
	}

	public void checkLeftMost(int left) {
		leftMostPosition = left < leftMostPosition ? left : leftMostPosition;
	}

	public void checkRightMost(int right) {
		rightMostPosition = right > rightMostPosition ? right
				: rightMostPosition;
	}

	@Override
	public int compareTo(SnippetWindow o) {
		if ((distinctMatches < o.distinctMatches)
				|| (distinctMatches == o.distinctMatches)
						&& ((numberOfMatches < o.numberOfMatches)
								|| (numberOfMatches == o.numberOfMatches)
										&& (getMiddleAlign() > o
												.getMiddleAlign()))) {
			return 1;
		}
		return -1;
	}

	public List<Integer> getMatches() {
		return matches;
	}

	private int getMiddleAlign() {
		int right = SnippetGenerator.MAX_WINDOW_LENGTH - rightMostPosition - 1;
		return leftMostPosition * leftMostPosition + right * right;
	}

	public int getPosition() {
		return pos;
	}
}
