package de.hpi.ir.yahoogle.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.hpi.ir.yahoogle.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.Tokenizer;

public class TokenizerTest {

	@Before
	public void setUp() throws Exception {
		new SearchEngineYahoogle(); // to initialize teamDirectory... I know,
									// it's dumb but we can't change it
	}

	@Test
	public void testCount() {
		Tokenizer t = new Tokenizer("foo bar baz", false);
		assertEquals(3, t.countTokens());
		t = new Tokenizer("foo bar-bar baz", false);
		assertEquals(4, t.countTokens());
		t = new Tokenizer("foo bar baz", false);
		assertEquals(3, t.countTokens());
		assertEquals("foo", t.next());
		assertEquals(3, t.countTokens());
		assertEquals("bar", t.next());
	}
}
