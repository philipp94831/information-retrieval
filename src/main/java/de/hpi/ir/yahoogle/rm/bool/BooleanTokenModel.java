package de.hpi.ir.yahoogle.rm.bool;

import java.util.List;
import java.util.Set;

import de.hpi.ir.yahoogle.index.Index;

public class BooleanTokenModel extends BooleanModel {

	public BooleanTokenModel(Index index) {
		super(index);
	}

	protected Set<Integer> find(List<String> phrases) {
		return index.find(phrases);
	}
}
