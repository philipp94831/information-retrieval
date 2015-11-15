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
		for (Integer docNumber : index.getAllDocNumbers()) {
			StringTokenizer tokenizer = new StringTokenizer(query);
			double result = 0.0;
			while (tokenizer.hasMoreTokens()) {
				String queryTerm = tokenizer.nextToken();
				int ld = index.wordCount(docNumber);
				int fi = index.wordCount(docNumber, queryTerm);
				int ci = index.wordCount(queryTerm);
				result += compute(fi, ld, ci);
			}
			results.put(docNumber, result);
		}
		return results;
	}

}
