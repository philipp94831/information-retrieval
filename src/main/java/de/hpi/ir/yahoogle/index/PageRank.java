package de.hpi.ir.yahoogle.index;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PageRank {

	private static final Logger LOGGER = Logger
			.getLogger(PageRank.class.getName());
	private static final double LAMBDA = 0.15;
	private final Map<Integer, List<Integer>> cites;
	private Map<Integer, Integer> outLinks;
	private Map<Integer, Double> pageRank;

	public PageRank(Map<Integer, List<Integer>> cites) {
		this.cites = cites;
		initialize();
	}

	private void initialize() {
		outLinks = cites.values().stream().flatMap(Collection::stream)
				.collect(Collectors.groupingBy(Function.identity())).entrySet()
				.stream().collect(Collectors.toMap(Entry::getKey,
						e -> e.getValue().size()));
	}

	public Map<Integer, Double> compute() {
		pageRank = new HashMap<>();
		boolean converged = false;
		int i = 0;
		while (!converged) {
			Map<Integer, Double> newPageRank = computeNewPageRank();
			double delta = getDiff(newPageRank);
			LOGGER.info("Delta after " + ++i + " iteration" + (i > 1 ? "s" : "")
					+ ": " + delta);
			pageRank = newPageRank;
			converged = delta < Math.pow(10, -17) || (i > 200);
		}
		return pageRank;
	}

	private double getDiff(Map<Integer, Double> newPageRank) {
		double diff = Math.sqrt(newPageRank.entrySet().stream()
				.mapToDouble(e -> e.getValue()
						- pageRank.getOrDefault(e.getKey(), 0.0))
				.map(d -> d * d).sum());
		return diff;
	}

	private Map<Integer, Double> computeNewPageRank() {
		Map<Integer, Double> newPageRank = new HashMap<>();
		for (Integer docNumber : cites.keySet()) {
			newPageRank.put(docNumber, compute(docNumber));
		}
		normalize(newPageRank);
		return newPageRank;
	}

	private void normalize(Map<Integer, Double> newPageRank) {
		double length = Math.sqrt(
				newPageRank.values().stream().mapToDouble(d -> d * d).sum());
		newPageRank.entrySet().forEach(e -> e.setValue(e.getValue() / length));
	}

	private double compute(Integer docNumber) {
		List<Integer> citedFrom = cites.get(docNumber);
		double inScore = 0.0;
		for (Integer citation : citedFrom) {
			if (!cites.containsKey(citation)) {
				continue;
			}
			Double p = pageRank.getOrDefault(citation, 1.0);
			Integer o = outLinks.get(citation);
			inScore += p / o;
		}
		return LAMBDA / cites.size() + (1 - LAMBDA) * inScore;
	}
}
