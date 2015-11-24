package de.hpi.ir.yahoogle.rm;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
	public Map<Integer, Double> compute(String query) {
		Map<Integer, Double> results = new HashMap<Integer, Double>();
		StringTokenizer t = new StringTokenizer(query);
		int[] cis = new int[t.countTokens()];
		for(int i = 0; t.hasMoreTokens(); i++) {
			cis[i] = index.wordCount(t.nextToken());
		}
		for (Integer docNumber : index.find(query)) {
			StringTokenizer tokenizer = new StringTokenizer(query);
			double result = 0.0;
			int ld = index.wordCount(docNumber);
			for(int i = 0; tokenizer.hasMoreTokens(); i++) {
				String queryTerm = tokenizer.nextToken();
				int fi = index.wordCount(docNumber, queryTerm);
				result += compute(fi, ld, cis[i]);
			}
			results.put(docNumber, result);
		}
		return results;
	}

}
