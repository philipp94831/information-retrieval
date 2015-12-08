package de.hpi.ir.yahoogle;

public class SnippetWindow implements Comparable<SnippetWindow> {

	int distinctMatches = 0;
	int leftMostPosition = SnippetGenerator.MAX_WINDOW_LENGTH;
	int matches = 0;
	int pos;
	int rightMostPosition = 0;

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
		rightMostPosition = right > rightMostPosition ? right : rightMostPosition;
	}

	@Override
	public int compareTo(SnippetWindow o) {
		if ((distinctMatches < o.distinctMatches) || (distinctMatches == o.distinctMatches) && ((matches < o.matches) || (matches == o.matches) && (getMiddleAlign() > o.getMiddleAlign()))) {
			return -1;
		}
		return 1;
	}

	public int getMiddleAlign() {
		int right = SnippetGenerator.MAX_WINDOW_LENGTH - rightMostPosition - 1;
		return leftMostPosition * leftMostPosition + right * right;
	}

	public int getPosition() {
		return pos;
	}

}
