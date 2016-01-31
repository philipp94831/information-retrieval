package de.hpi.ir.yahoogle.rm.ql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.parsing.PatentPart;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.rm.Model;
import de.hpi.ir.yahoogle.util.ValueComparator;

public class QLModel extends Model<QLResult> {

	private final double lambda;
	private final int lc;
	private int[] cis;
	private Set<Integer> all;
	private List<Map<Integer, Set<Integer>>> found;

	public QLModel(Index index) {
		this(index, 0.2);
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

	public List<QLResult> compute(List<String> query) {
		initializeModel(query);
		return computeAll();
	}

	private void initializeModel(List<String> query) {
		phrases = query;
		found = findPatents();
		cis = computeCi();
		Map<Integer, Integer> totalHits = getTotalHitsPerPatent();
		all = getAllCandidates(totalHits);
	}

	private List<QLResult> computeAll() {
		List<QLResult> res = all.stream().map(this::computeForPatent).collect(Collectors.toList());
		return res;
	}

	private List<Map<Integer, Set<Integer>>> findPatents() {
		return phrases.stream().map(index::findPositions).collect(Collectors.toList());
	}

	private int[] computeCi() {
		int[] cis = new int[phrases.size()];
		for (int i = 0; i < phrases.size(); i++) {
			cis[i] = found.get(i).values().stream().mapToInt(Set::size).sum();
		}
		return cis;
	}

	private QLResult computeForPatent(Integer docNumber) {
		QLResult result = new QLResult(docNumber);
		PatentResume resume = index.getPatent(docNumber);
		int ld = resume.getWordCount();
		double score = 0.0;
		for (int i = 0; i < phrases.size(); i++) {
			Set<Integer> positions = found.get(i).getOrDefault(docNumber, new HashSet<>());
			result.addPositions(phrases.get(i), positions);
			double fi = positions.stream().mapToDouble(pos -> partWeight(resume.getPartAtPosition(pos))).sum();
			score += compute(fi, ld, cis[i]);
		}
//		score /= Math.pow(resume.getPageRank(), 0.1);
		result.setScore(score);
		return result;
	}

	private Set<Integer> getAllCandidates(Map<Integer, Integer> totalHits) {
		if (totalHits.isEmpty()) {
			return new HashSet<>();
		}
		TreeMap<Integer, Integer> sorted = ValueComparator.sortByValueDescending(totalHits);
		int minHits = new ArrayList<>(sorted.entrySet()).get(Math.min(topK, sorted.size() - 1)).getValue();
		return totalHits.entrySet().stream().filter(e -> e.getValue() >= minHits).map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	private Map<Integer, Integer> getTotalHitsPerPatent() {
		return found.stream().map(Map::entrySet).flatMap(Collection::stream).collect(
				Collectors.groupingBy(Entry::getKey, Collectors.reducing(0, e -> e.getValue().size(), Integer::sum)));
	}

	private static double partWeight(PatentPart part) {
		switch (part) {
		case TITLE:
			return 5.0;
		case ABSTRACT:
			return 2.0;
		case DESCRIPTION:
			return 1.0;
		case CLAIM:
			return 1.2;
		default:
			return 0.0;
		}
	}

	@Override
	public List<QLResult> compute(String query) {
		return compute(QueryProcessor.extractPhrases(query));
	}
}
