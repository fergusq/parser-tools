package org.kaivos.tests.parsertools;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.kaivos.parsertools.ParserRule;
import org.kaivos.sc.ITokenList;
import org.kaivos.sc.Token;
import org.kaivos.sc.TokenList;

public class ParserRuleTest {
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetFirstToken() {
		
		//fail("Not yet implemented");
		{
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{"test"}});
			
			Set<String> ft = rule1.getFirstToken();
			if (ft.size() != 1 || !ft.toArray()[0].equals("test")) fail("Wrong FT: " + ft);
		}
		{
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{new Object[][] {{"test"}}, "test1"}});
			
			Set<String> ft = rule1.getFirstToken();
			if (ft.size() != 1 || !ft.toArray()[0].equals("test")) fail("Wrong FT: " + ft);
		}
		{
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{new Object[][] {{"test"}}, "test1"}});
			ParserRule rule2 = new ParserRule.Rule(new Object[][] {{rule1, "test1"}});
			
			Set<String> ft = rule2.getFirstToken();
			if (ft.size() != 1 || !ft.toArray()[0].equals("test")) fail("Wrong FT: " + ft);
		}
		
	}
	
	@Test
	public void testMatch( ) {
		{
			ITokenList list = new TokenList(Arrays.asList(new Token("test", 1, null), new Token("test1", 1, null), new Token("<EOF>", 1, null)));
			
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{"test", "test1"}});
			
			assertTrue("Rule does not match!", rule1.match(list));
		}
		
		{
			ITokenList list = new TokenList(Arrays.asList(new Token("test", 1, null), new Token("test1", 1, null), new Token("<EOF>", 1, null)));
			
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{"test", "test2"}});
			
			assertFalse("Rule does match!", rule1.match(list));
		}
		
		{
			ITokenList list = new TokenList(Arrays.asList(new Token("test", 1, null), new Token("test1", 1, null), new Token("<EOF>", 1, null)));
			
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{"test", "test1"}, {"test", "test2"}});
			
			assertTrue("Rule does not match!", rule1.match(list));
		}
		
		{
			ITokenList list = new TokenList(Arrays.asList(new Token("test", 1, null), new Token("test2", 1, null), new Token("<EOF>", 1, null)));
			
			ParserRule rule1 = new ParserRule.Rule(new Object[][] {{"test", "test1"}, {"test", "test2"}});
			
			assertTrue("Rule does not match!", rule1.match(list));
		}
	}

}
