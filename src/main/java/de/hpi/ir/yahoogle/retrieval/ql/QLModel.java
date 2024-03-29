package de.hpi.ir.yahoogle.retrieval.ql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MinMaxPriorityQueue;

import de.hpi.ir.yahoogle.index.DocumentPosting;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.index.PatentResume;
import de.hpi.ir.yahoogle.index.search.PhraseResultIterator;
import de.hpi.ir.yahoogle.parsing.PatentPart;
import de.hpi.ir.yahoogle.retrieval.Model;
import de.hpi.ir.yahoogle.search.QueryProcessor;

public class QLModel extends Model<QLResult> {

	private static final Logger LOGGER = Logger
			.getLogger(QLModel.class.getName());
	private final double lambda;
	private final int lc;
	private int[] cis;
	private Set<Integer> all;
	private List<Map<Integer, byte[]>> found;
	private final Map<Integer, Integer> totalHits = new HashMap<>();
	private Set<Integer> whiteList;
	private List<String> phrases;

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

	private List<QLResult> compute(List<String> query) {
		initializeModel(query);
		return computeAll();
	}

	private void initializeModel(List<String> phrases) {
		this.phrases = phrases;
		found = findPatents();
		results = totalHits.size();
		all = getAllCandidates();
	}

	private List<QLResult> computeAll() {
		MinMaxPriorityQueue<QLResult> result = MinMaxPriorityQueue
				.maximumSize(topK).create();
		all.stream().forEach(i -> result.add(computeForPatent(i)));
		return new ArrayList<>(result);
	}

	private List<Map<Integer, byte[]>> findPatents() {
		cis = new int[phrases.size()];
		List<Map<Integer, byte[]>> listOfResults = new ArrayList<>();
		int i = 0;
		for (String phrase : phrases) {
			cis[i] = 0;
			Map<Integer, byte[]> result = new HashMap<>();
			PhraseResultIterator iterator = index.findPositions(phrase);
			while (iterator.hasNext()) {
				DocumentPosting d = iterator.next();
				cis[i] += d.getAll().size();
				if (whiteList != null
						&& !whiteList.contains(d.getDocNumber())) {
					continue;
				}
				totalHits.merge(d.getDocNumber(), d.getAll().size(),
						Integer::sum);
				try {
					result.put(d.getDocNumber(), d.toByteArray());
				} catch (IOException e) {
					LOGGER.severe("Error encoding DocumentPosting "
							+ d.getDocNumber());
				}
			}
			listOfResults.add(result);
			i++;
		}
		return listOfResults;
	}

	private QLResult computeForPatent(Integer docNumber) {
		QLResult result = new QLResult(docNumber);
		PatentResume resume = index.getPatent(docNumber);
		int ld = resume.getWordCount();
		double score = 0.0;
		for (int i = 0; i < phrases.size(); i++) {
			Set<Integer> positions = new HashSet<>();
			byte[] bytes = found.get(i).remove(docNumber);
			if (bytes != null) {
				positions = DocumentPosting.fromBytes(bytes).getAll();
			}
			result.addPositions(phrases.get(i), positions);
			double fi = positions.stream()
					.mapToDouble(
							pos -> partWeight(resume.getPartAtPosition(pos)))
					.sum();
			score += compute(fi, ld, cis[i]);
		}
		// score /= Math.pow(resume.getPageRank(), 0.1);
		result.setScore(score);
		return result;
	}

	private Set<Integer> getAllCandidates() {
		if (totalHits.isEmpty()) {
			return new HashSet<>();
		}
		List<Integer> hits = new ArrayList<>(totalHits.values());
		Collections.sort(hits, Collections.reverseOrder());
		int minHits = hits.get(Math.min(topK * 1000, hits.size() - 1));
		return totalHits.entrySet().stream()
				.filter(e -> e.getValue() >= minHits).map(Entry::getKey)
				.collect(Collectors.toSet());
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

	public void setWhiteList(Set<Integer> whiteList) {
		this.whiteList = whiteList;
	}
}
