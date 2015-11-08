package de.hpi.ir.yahoogle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InMemoryIndex {

	private Map<String, YahoogleTokenMap> tokenMap = new HashMap<String, YahoogleTokenMap>();
	private LinkedRandomAccessIndex linkedIndex;	

	public InMemoryIndex(LinkedRandomAccessIndex linkedIndex) {
		this.linkedIndex = linkedIndex;
	}

	public void buffer(String token, int docNumber, YahoogleIndexPosting posting) {
		if (tokenMap.get(token) == null) {
			tokenMap.put(token, new YahoogleTokenMap());
		}
		tokenMap.get(token).add(docNumber, posting);
	}

	public void flush() {
		try {
			for (Entry<String, YahoogleTokenMap> entry : tokenMap.entrySet()) {
				linkedIndex.add(entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokenMap.clear();
	}
	
}
