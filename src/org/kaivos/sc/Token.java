package org.kaivos.sc;

public class Token {
	public String token;
	public int line;
	public String file;
	
	public Token(String t, int l, String f) {
		this.token = t;
		this.line = l;
		this.file = f;
	}
}