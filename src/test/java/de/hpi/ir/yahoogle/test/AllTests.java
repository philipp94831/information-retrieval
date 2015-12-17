package de.hpi.ir.yahoogle.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EliasDeltaReadWriteTest.class, ByteReadWriteTest.class,
		SnippetWindowTest.class, TokenizerTest.class, VByteReadWriteTest.class })
public class AllTests {
}
