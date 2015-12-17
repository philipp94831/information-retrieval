package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.io.VByteReader;

public class BinaryPostingList implements Comparable<BinaryPostingList> {

	private byte[] bytes;
	private final String token;

	public BinaryPostingList(String token, byte[] bytes) {
		this.token = token;
		this.bytes = bytes;
	}

	public void append(byte[] bytes2) throws IOException {
		ByteWriter out = new ByteWriter();
		out.write(bytes);
		out.write(bytes2);
		bytes = out.toByteArray();
	}

	@Override
	public int compareTo(BinaryPostingList o) {
		int comp = token.compareTo(o.token);
		return comp == 0 ? 1 : comp;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public Map<Integer, Set<Integer>> getDocumentsWithPositions() throws IOException {
		Map<Integer, Set<Integer>> result = new HashMap<>();
		int i = 0;
		while (i < bytes.length) {
			AbstractReader in = new ByteReader(bytes, i, 2 * Integer.BYTES);
			i += 2 * Integer.BYTES;
			int docNumber = in.readInt();
			int bsize = in.readInt();
			in = new VByteReader(bytes, i, bsize);
			Set<Integer> pos = new HashSet<>();
			int oldPos = 0;
			while (in.hasLeft()) {
				int p = in.readInt();
				pos.add(oldPos + p);
				oldPos += p;
			}
			result.put(docNumber, pos);
			i += bsize;
		}
		return result;
	}

	public String getToken() {
		return token;
	}
}
