package org.kaivos.parsertools;

import org.kaivos.sc.TokenScanner;

public class TestParser {

	Object[][] rule2 = {{"hyvä"}};
	Object[][] rule3 = {{"paha"}, {"pahuus"}};
	
	Object[][] rule1 = {{"+", rule2}, {"-", rule3}};

	
	public void parse(TokenScanner s) {
		
	}
	
}
