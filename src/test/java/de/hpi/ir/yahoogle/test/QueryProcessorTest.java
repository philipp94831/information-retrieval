package de.hpi.ir.yahoogle.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import SearchEngine.SearchEngineYahoogle;
import de.hpi.ir.yahoogle.language.StopWordList;
import de.hpi.ir.yahoogle.query.QueryProcessor;
import de.hpi.ir.yahoogle.query.QueryType;


public class QueryProcessorTest {

	@Before
	public void setUp() throws Exception {
		new SearchEngineYahoogle(); // to initialize teamDirectory... I know,
									// it's dumb but we can't change it
	}

	@Test
	public void test() {
		String stopWord = "comprises";
		assertTrue(StopWordList.isStopword(stopWord));
		assertEquals(QueryType.BOOLEAN, QueryProcessor.getQueryType(stopWord + " AND foo"));
		assertEquals(QueryType.RELEVANT, QueryProcessor.getQueryType(stopWord));
		assertEquals(QueryType.RELEVANT, QueryProcessor.getQueryType("foo bar"));
		assertEquals(QueryType.RELEVANT, QueryProcessor.getQueryType("fooandbar"));
		assertEquals(QueryType.RELEVANT, QueryProcessor.getQueryType("\"foo and bar\""));
		assertEquals(QueryType.RELEVANT, QueryProcessor.getQueryType("foo \"and\" bar"));
		assertEquals(QueryType.BOOLEAN, QueryProcessor.getQueryType("LinkTo:12345678"));
		assertEquals(QueryType.BOOLEAN, QueryProcessor.getQueryType("NOT " + stopWord));
	}
}
