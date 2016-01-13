package de.hpi.ir.yahoogle.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Test;

import de.hpi.ir.yahoogle.snippets.SnippetWindow;

public class SnippetWindowTest {

	@Test
	public void testCompare() {
		SnippetWindow window1 = new SnippetWindow(0);
		SnippetWindow window2 = new SnippetWindow(0);
		window1.addDistinctMatch();
		assertTrue(window1.compareTo(window2) < 0);
		window2.addDistinctMatch();
		window1.addMatches(new TreeSet<Integer>(Arrays.asList(1)));
		assertTrue(window1.compareTo(window2) < 0);
		window2.addMatches(new TreeSet<Integer>(Arrays.asList(1)));
		window1.checkLeftMost(4);
		window1.checkRightMost(5);
		window2.checkLeftMost(3);
		window2.checkRightMost(4);
		assertTrue(window1.compareTo(window2) < 0);
		window2.checkRightMost(5);
		assertTrue(window1.compareTo(window2) > 0);
	}
}
