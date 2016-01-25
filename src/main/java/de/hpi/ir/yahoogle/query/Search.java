package de.hpi.ir.yahoogle.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hpi.ir.WebFile;
import de.hpi.ir.yahoogle.index.Index;
import de.hpi.ir.yahoogle.rm.Result;
import de.hpi.ir.yahoogle.snippets.SnippetGenerator;

public abstract class Search {
	
	protected static final int ALL_RESULTS = -1;
	
	protected final Index index;
	protected String query;
	protected int topK = ALL_RESULTS;
	
	public void setTopK(int topK) {
		this.topK = topK;
	}

	public Search(Index index, String query) {
		this.index = index;
		this.query = query;
	}

	private static double computeGain(int goldRank) {
		return 1 + Math.floor(10 * Math.pow(0.5, 0.1 * goldRank));
	}
	
	public abstract ArrayList<String> search();

	protected Map<Integer, String> generateSnippets(Collection<? extends Result> results, List<String> phrases) {
		SnippetGenerator generator = new SnippetGenerator(phrases);
		Map<Integer, String> snippets = new HashMap<>();
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			String snippet = generator.generate(result, index.getPatent(docNumber));
			snippets.put(docNumber, snippet);
		}
		return snippets;
	}

	private static String toGoogleQuery(String query) {
		return query.toLowerCase().replaceAll("\\snot\\s", " -").replaceAll("^not\\s", "-");
	}

	protected Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		double originalDcg = 0.0;
		double goldDcg = 0.0;
		for (int i = 0; i < p; i++) {
			String originalRank = ranking.get(i);
			int goldRank = goldRanking.indexOf(originalRank) + 1;
			double originalGain = goldRank == 0 ? 0 : computeGain(goldRank);
			double goldGain = computeGain(i + 1);
			if (i == 0) {
				originalDcg = originalGain;
				goldDcg = goldGain;
			} else {
				originalDcg += originalGain * Math.log(2) / Math.log(i + 1);
				goldDcg += goldGain * Math.log(2) / Math.log(i + 1);
			}
		}
		return originalDcg / goldDcg;
	}

	protected ArrayList<String> generateOutput(Collection<? extends Result> results, Map<Integer, String> snippets,
			String query) {
		ArrayList<String> output = new ArrayList<>();
		String googleQuery = toGoogleQuery(query);
		ArrayList<String> goldRanking = new WebFile().getGoogleRanking(googleQuery);
		ArrayList<String> originalRanking = new ArrayList<>(
				results.stream().map(r -> Integer.toString(r.getDocNumber())).collect(Collectors.toList()));
		int i = 1;
		for (Result result : results) {
			int docNumber = result.getDocNumber();
			double ndcg = computeNdcg(goldRanking, originalRanking, i);
			output.add(
					String.format("%08d", docNumber) + "\t" + index.getPatent(docNumber).getPatent().getInventionTitle()
							+ "\t" + ndcg + "\n" + snippets.get(docNumber));
			i++;
		}
		return output;
	}

	protected ArrayList<String> generateSlimOutput(Collection<? extends Result> r) {
		ArrayList<String> results = new ArrayList<>();
		for (Result result : r) {
			results.add(String.format("%08d", result.getDocNumber()) + "\t"
					+ index.getPatent(result.getDocNumber()).getPatent().getInventionTitle());
		}
		return results;
	}
}
