package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.hpi.ir.yahoogle.io.ByteWriter;
import de.hpi.ir.yahoogle.util.MergeSortIterator;

public class PartitionedBinaryPostingList {

	private static final int MAX_SIZE = 16 * 1024 * 1024;
	private final List<BinaryPostingList> bytes = new ArrayList<>();
	private final String token;

	public PartitionedBinaryPostingList(BinaryPostingList postingList) {
		this.token = postingList.getToken();
		add(postingList);
	}

	public void add(BinaryPostingList newBytes) {
		bytes.add(newBytes);
	}

	public List<byte[]> getSortedBlocks() throws IOException {
		MergeSortIterator<BinaryPostingList, DocumentPosting, Integer> documents = new MergeSortIterator<>(
				bytes);
		List<byte[]> blocks = new ArrayList<>();
		ByteWriter out = new ByteWriter(MAX_SIZE);
		int old = 0;
		while (documents.hasNext()) {
			List<DocumentPosting> documentList = documents.next();
			for (DocumentPosting document : documentList) {
				if (old > document.getDocNumber()) {
					throw new RuntimeException("DocumentPostings not ordered");
				}
				old = document.getDocNumber();
				byte[] b = document.toByteArray();
				if (b.length + Integer.BYTES <= out.spaceLeft()) {
					out.writeInt(b.length);
					out.write(b);
				} else {
					blocks.add(out.toByteArray());
					out = new ByteWriter(MAX_SIZE);
				}
			}
		}
		byte[] remaining = out.toByteArray();
		if (remaining.length > 0) {
			blocks.add(out.toByteArray());
		}
		return blocks;
	}

	public String getToken() {
		return token;
	}
}
