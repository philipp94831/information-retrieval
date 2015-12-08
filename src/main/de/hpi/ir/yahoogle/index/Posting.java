package de.hpi.ir.yahoogle.index;

public class Posting implements Comparable<Posting> {

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
