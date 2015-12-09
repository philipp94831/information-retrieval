package de.hpi.ir.yahoogle.rm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.parsing.PatentPart;

public class QLModel extends Model {

	private final double lambda;
	private final int lc;
	private Map<PatentPart, Double> partWeights = new HashMap<PatentPart, Double>();

	public QLModel(Index index) {
		this(index, 0.2);
		partWeights.put(PatentPart.TITLE, 1.2);
		partWeights.put(PatentPart.ABSTRACT, 1.0);
	}

	public QLModel(Index index, double lambda) {
		super(index);
		this.lambda = lambda;
		this.lc = index.wordCount();
	}

	private double compute(double fi, int ld, int ci) {
		double first = (1 - lambda) * fi / ld;
		double second = lambda * ci / lc;
		return Math.log(first + second);
	}

	@Override
	public List<ModelResult> compute(List<String> query) {
		List<ModelResult> results = new ArrayList<ModelResult>();
		int[] cis = new int[query.size()];
		List<Map<Integer, Set<Integer>>> found = new ArrayList<Map<Integer, Set<Integer>>>();
		for (int i = 0; i < query.size(); i++) {
			String phrase = query.get(i);
			Map<Integer, Set<Integer>> result = index.findWithPositions(phrase);
			found.add(result);
			cis[i] = result.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		}
		Set<Integer> all = found.stream().map(m -> m.keySet()).flatMap(Collection::stream).collect(Collectors.toSet());
		for (Integer docNumber : all) {
			ModelResult result = new ModelResult(docNumber);
			double score = 0.0;
			int ld = index.wordCount(docNumber);
			for (int i = 0; i < query.size(); i++) {
				Set<Integer> list = found.get(i).getOrDefault(docNumber, new HashSet<Integer>());
				result.addPositions(query.get(i), list);
				PatentResume resume = index.getPatent(docNumber);
				double fi = list.stream().mapToDouble(pos -> partWeights.get(resume.getPartAtPosition(pos))).sum();
				score += compute(fi, ld, cis[i]);
			}
			result.setScore(score);
			results.add(result);
		}
		return results;
	}

}
