package de.hpi.ir.yahoogle.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class ByteReadWriteTest {

	@Test
	public void testReadAndWriteInt() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
		ByteWriter out = new ByteWriter();
		try {
			for (Integer i : list) {
				out.writeInt(i);
			}
			byte[] bytes = out.toByteArray();
			ByteReader in = new ByteReader(bytes, 0, bytes.length);
			List<Integer> decoded = new ArrayList<Integer>();
			while (in.hasLeft()) {
				decoded.add(in.readInt());
			}
			assertEquals(list.size(), decoded.size());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.get(i), decoded.get(i));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testReadAndWriteShort() {
		List<Short> list = new ArrayList<Short>(Arrays.asList((short) 1, (short) 2, (short) 3));
		ByteWriter out = new ByteWriter();
		try {
			for (Short i : list) {
				out.writeShort(i);
			}
			byte[] bytes = out.toByteArray();
			ByteReader in = new ByteReader(bytes, 0, bytes.length);
			List<Short> decoded = new ArrayList<Short>();
			while (in.hasLeft()) {
				decoded.add(in.readShort());
			}
			assertEquals(list.size(), decoded.size());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.get(i), decoded.get(i));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
