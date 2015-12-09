package de.hpi.ir.yahoogle.rm;

import java.util.Comparator;

public class ModelResultComparator implements Comparator<ModelResult> {

	@Override
	public int compare(ModelResult o1, ModelResult o2) {
		return -Double.compare(o1.getScore(), o2.getScore());
	}
}
