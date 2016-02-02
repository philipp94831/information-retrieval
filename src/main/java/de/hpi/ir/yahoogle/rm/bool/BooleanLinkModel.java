package de.hpi.ir.yahoogle.rm.bool;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;

public class BooleanLinkModel extends BooleanModel {

	public BooleanLinkModel(Index index) {
		super(index);
	}
	
	@Override
	protected Set<Integer> find(List<String> phrases) {
		phrases = phrases.stream().map(p -> p.replaceAll("LinkTo:", "")).collect(Collectors.toList());
		return index.findLinks(phrases);	
	}
}
