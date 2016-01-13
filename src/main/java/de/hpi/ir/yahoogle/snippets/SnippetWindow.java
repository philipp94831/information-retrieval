package de.hpi.ir.yahoogle.snippets;

import java.util.NavigableSet;

public class SnippetWindow implements Comparable<SnippetWindow> {

	private int distinctMatches = 0;
	private int leftMostPosition = SnippetGenerator.MAX_WINDOW_LENGTH;
	private int numberOfMatches = 0;
	private final int pos;
	private int rightMostPosition = 0;
	private NavigableSet<Integer> matches;

	public SnippetWindow(int pos) {
		this.pos = pos;
	}

	public void addDistinctMatch() {
		distinctMatches++;
	}
	
	public NavigableSet<Integer> getMatches() {
		return matches;
	}

	public void addMatches(NavigableSet<Integer> matches) {
		this.matches = matches;
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
						&& ((numberOfMatches < o.numberOfMatches) || (numberOfMatches == o.numberOfMatches)
								&& (getMiddleAlign() > o.getMiddleAlign()))) {
			return 1;
		}
		return -1;
	}

	private int getMiddleAlign() {
		int right = SnippetGenerator.MAX_WINDOW_LENGTH - rightMostPosition - 1;
		return leftMostPosition * leftMostPosition + right * right;
	}

	public int getPosition() {
		return pos;
	}
}
