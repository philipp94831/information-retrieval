package de.hpi.ir.yahoogle.snippets;

public class SnippetWindow implements Comparable<SnippetWindow> {

	private int distinctMatches = 0;
	private int leftMostPosition = SnippetGenerator.MAX_WINDOW_LENGTH;
	private int matches = 0;
	private final int pos;
	private int rightMostPosition = 0;

	public SnippetWindow(int pos) {
		this.pos = pos;
	}

	public void addDistinctMatch() {
		distinctMatches++;
	}

	public void addMatches(int matches) {
		this.matches += matches;
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
						&& ((matches < o.matches) || (matches == o.matches)
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
