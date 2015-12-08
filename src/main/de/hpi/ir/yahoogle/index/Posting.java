package de.hpi.ir.yahoogle.index;

import java.io.Serializable;

public class Posting implements Serializable, Comparable<Posting> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 406969836377709119L;
	private int position;

	@Override
	public int compareTo(Posting o) {
		return Integer.compare(position, o.position);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
