package de.hpi.ir.yahoogle.evaluation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ndcg {

	private static double computeGain(int goldRank) {
		return 1 + Math.floor(10 * Math.pow(0.5, 0.1 * goldRank));
	}

	private static String extractDocNumber(String result) {
		Matcher m = Pattern.compile("^(\\d+)\t").matcher(result);
		if (m.find()) {
			return Integer.toString(Integer.parseInt(m.group(1)));
		}
		return null;
	}

	private final ArrayList<String> goldRanking;
	private final ArrayList<String> ranking;

	public Ndcg(ArrayList<String> goldRanking, ArrayList<String> ranking) {
		this.goldRanking = goldRanking;
		this.ranking = ranking;
	}

	public Double at(int p) {
		double originalDcg = 0.0;
		double goldDcg = 0.0;
		for (int i = 1; i <= p; i++) {
			double originalGain = computeOriginalGain(i);
			double goldGain = computeGain(i);
			if (i == 1) {
				originalDcg = originalGain;
				goldDcg = goldGain;
			} else {
				originalDcg += originalGain * Math.log(2) / Math.log(i);
				goldDcg += goldGain * Math.log(2) / Math.log(i);
			}
		}
		return originalDcg / goldDcg;
	}

	private double computeOriginalGain(int i) {
		double gain = 0.0;
		if (i <= ranking.size()) {
			String docNumber = extractDocNumber(ranking.get(i - 1));
			if (docNumber != null) {
				int goldRank = goldRanking.indexOf(docNumber) + 1;
				if (goldRank > 0) {
					gain = computeGain(goldRank);
				}
			}
		}
		return gain;
	}
}
