package de.hpi.ir.yahoogle.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PhraseResultIterator implements Iterator<DocumentPosting> {

	private List<TokenResultIterator> sources;
	private TokenResultIterator first;
	private Map<Integer, DocumentPosting> storage = new HashMap<>();
	private DocumentPosting currentNext;

	public PhraseResultIterator(List<TokenResultIterator> sources) {
		first = sources.get(0);
		sources.remove(0);
		this.sources = sources;
		fetchNext();
	}

	private void fetchNext() {
		currentNext = null;
		while (first.hasNext()) {
			DocumentPosting base = first.next();
			boolean invalid = false;
			for (int i = 0; i < sources.size(); i++) {
				if (invalid) {
					continue;
				}
				DocumentPosting next;
				while (true) {
					next = nextPosting(i);
					if (next == null || base.compareTo(next) <= 0) {
						break;
					}
				}
				if (next == null || base.compareTo(next) < 0) {
					storage.put(i, next);
					invalid = true;
				} else {
					base.merge(next, i + 1);
				}
			}
			if (!invalid) {
				currentNext = base;
				return;
			}
		}
	}

	private DocumentPosting nextPosting(int i) {
		DocumentPosting result = storage.remove(i);
		if (result == null) {
			TokenResultIterator source = sources.get(i);
			if (source.hasNext()) {
				result = source.next();
			}
		}
		return result;
	}

	@Override
	public boolean hasNext() {
		return currentNext != null;
	}

	@Override
	public DocumentPosting next() {
		DocumentPosting next = currentNext;
		fetchNext();
		return next;
	}
}
