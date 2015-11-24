package de.hpi.ir.yahoogle.rm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;

public class QLModel extends Model {

	private final double lambda;
	private final int lc;

	public QLModel(Index index) {
		this(index, 0.2);
	}

	public QLModel(Index index, double lambda) {
		super(index);
		this.lambda = lambda;
		this.lc = index.wordCount();
	}

	private double compute(int fi, int ld, int ci) {
		double first = (1 - lambda) * fi / ld;
		double second = lambda * ci / lc;
		return Math.log(first + second);
	}

	@Override
	public Map<Integer, Double> compute(List<String> query) {
		Map<Integer, Double> results = new HashMap<Integer, Double>();
		int[] cis = new int[query.size()];
		List<Set<Integer>> found = new ArrayList<Set<Integer>>();
		for(int i = 0; i < query.size(); i++) {
			String phrase = query.get(i);
			cis[i] = index.wordCount(phrase);
			found.add(index.find(phrase));
		}
		Set<Integer> all = found.stream().flatMap(Collection::stream).collect(Collectors.toSet());
		for (Integer docNumber : all) {
			double result = 0.0;
			int ld = index.wordCount(docNumber);
			for(int i = 0; i < query.size(); i++) {
				int fi = index.wordCount(docNumber, query.get(i));
				result += compute(fi, ld, cis[i]);
			}
			results.put(docNumber, result);
		}
		return results;
	}

}
