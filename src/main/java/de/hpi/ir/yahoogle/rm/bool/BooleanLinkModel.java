package de.hpi.ir.yahoogle.rm.bool;

import java.util.List;
import java.util.Set;

import de.hpi.ir.yahoogle.index.Index;

public class BooleanLinkModel extends BooleanModel {

	public BooleanLinkModel(Index index) {
		super(index);
	}
	
	@Override
	protected Set<Integer> find(List<String> phrases) {
		return index.findLinks(phrases);		
	}
}
