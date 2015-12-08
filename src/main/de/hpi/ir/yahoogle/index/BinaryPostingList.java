package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class BinaryPostingList implements Comparable<BinaryPostingList> {
	
	private String token;
	private byte[] bytes;	

	public BinaryPostingList(String token, byte[] bytes) {
		this.token = token;
		this.bytes = bytes;
	}
	
	

	public String getToken() {
		return token;
	}



	public byte[] getBytes() {
		return bytes;
	}



	@Override
	public int compareTo(BinaryPostingList o) {
		return token.compareTo(o.token);
	}



	public void append(byte[] bytes2) throws IOException {
		ByteWriter out = new ByteWriter();
		out.write(bytes);
		out.write(bytes2);
		bytes = out.toByteArray();
	}
	

}
