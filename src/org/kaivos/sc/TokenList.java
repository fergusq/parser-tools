package org.kaivos.sc;

import java.util.List;

public class TokenList implements ITokenList {

	private List<Token> tokens;
	private int next = 0, line = 0;
	
	public TokenList(List<Token> l) {
		this.tokens = l;
		this.next = 0;
	}
	
	@Override
	public boolean hasNext() {
		return next <= tokens.size()-1;
	}

	@Override
	public String next() {
		Token nextS = tokens.get(next++);
		line = nextS.line;
		return nextS.token;
	}

	@Override
	public String seek() {
		Token nextS = tokens.get(next);
		return nextS.token;
	}

	@Override
	public String seek(int times) {
		Token nextS = tokens.get(next + times -1); 
		return nextS.token;
	}

	@Override
	public int line() {
		return line;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public int getNext() {
		return next;
	}

	public int index() {
		return getNext();
	}
	
	public void setNext(int next) {
		this.next = next;
	}
	
	public void setIndex(int i) {
		setNext(i);
	}

	@Override
	public String file() {
		return null;
	}

}
