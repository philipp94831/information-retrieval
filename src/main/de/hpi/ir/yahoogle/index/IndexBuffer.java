package de.hpi.ir.yahoogle.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class IndexBuffer {

	private Map<String, TokenIndexBuffer> tokenMap = new HashMap<String, TokenIndexBuffer>();
	private LinkedRandomAccessIndex linkedIndex;	

	public IndexBuffer(LinkedRandomAccessIndex linkedIndex) {
		this.linkedIndex = linkedIndex;
	}

	public void buffer(String token, int docNumber, IndexPosting posting) {
		if (tokenMap.get(token) == null) {
			tokenMap.put(token, new TokenIndexBuffer());
		}
		tokenMap.get(token).add(docNumber, posting);
	}

	public void flush() {
		try {
			for (Entry<String, TokenIndexBuffer> entry : tokenMap.entrySet()) {
				linkedIndex.add(entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokenMap.clear();
	}
	
}
