package de.hpi.ir.yahoogle.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.hpi.ir.yahoogle.io.AbstractReader;
import de.hpi.ir.yahoogle.io.AbstractWriter;
import de.hpi.ir.yahoogle.io.ByteReader;
import de.hpi.ir.yahoogle.io.ByteWriter;

public class ByteReadWriteTest {

	@Test
	public void testReadAndWriteInt() throws IOException {
		List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
		AbstractWriter out = new ByteWriter();
		for (Integer i : list) {
			out.writeInt(i);
		}
		byte[] bytes = out.toByteArray();
		AbstractReader in = new ByteReader(bytes, 0, bytes.length);
		List<Integer> decoded = new ArrayList<>();
		while (in.hasLeft()) {
			decoded.add(in.readInt());
		}
		assertEquals(list.size(), decoded.size());
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i), decoded.get(i));
		}
	}

	@Test
	public void testReadAndWriteLong() throws IOException {
		List<Long> list = new ArrayList<>(
				Arrays.asList(1L, 2L, 3L));
		AbstractWriter out = new ByteWriter();
		for (Long i : list) {
			out.writeLong(i);
		}
		byte[] bytes = out.toByteArray();
		AbstractReader in = new ByteReader(bytes, 0, bytes.length);
		List<Long> decoded = new ArrayList<>();
		while (in.hasLeft()) {
			decoded.add(in.readLong());
		}
		assertEquals(list.size(), decoded.size());
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i), decoded.get(i));
		}
	}

	@Test
	public void testReadAndWriteShort() throws IOException {
		List<Short> list = new ArrayList<>(
				Arrays.asList((short) 1, (short) 2, (short) 3));
		AbstractWriter out = new ByteWriter();
		for (Short i : list) {
			out.writeShort(i);
		}
		byte[] bytes = out.toByteArray();
		AbstractReader in = new ByteReader(bytes, 0, bytes.length);
		List<Short> decoded = new ArrayList<>();
		while (in.hasLeft()) {
			decoded.add(in.readShort());
		}
		assertEquals(list.size(), decoded.size());
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i), decoded.get(i));
		}
	}

	@Test
	public void testReadAndWriteUTF() throws IOException {
		List<String> list = new ArrayList<>(
				Arrays.asList("foo", "bar", "baz"));
		ByteWriter out = new ByteWriter();
		for (String i : list) {
			out.writeUTF(i);
		}
		byte[] bytes = out.toByteArray();
		ByteReader in = new ByteReader(bytes, 0, bytes.length);
		List<String> decoded = new ArrayList<>();
		while (in.hasLeft()) {
			decoded.add(in.readUTF());
		}
		assertEquals(list.size(), decoded.size());
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i), decoded.get(i));
		}
	}
}
