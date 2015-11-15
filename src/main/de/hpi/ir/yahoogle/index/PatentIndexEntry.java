package de.hpi.ir.yahoogle.index;

public class PatentIndexEntry {

	private PatentResume resume;
	private int wordCount;

	public PatentIndexEntry(PatentResume resume, int wordCount) {
		this.resume = resume;
		this.wordCount = wordCount;
	}

	public PatentResume getResume() {
		return resume;
	}

	public void setResume(PatentResume resume) {
		this.resume = resume;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

}
