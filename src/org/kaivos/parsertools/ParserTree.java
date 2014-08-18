package org.kaivos.parsertools;

import java.util.Arrays;

import org.kaivos.sc.ITokenList;
import org.kaivos.sc.TokenScanner;
import org.kaivos.stg.error.SyntaxError;
import org.kaivos.stg.error.UnexpectedTokenSyntaxError;

public class ParserTree {
	public static void accept(String token, ITokenList s) throws SyntaxError {
		if (!seek(s).equals(token)) {
			String t = next(s);
			throw new UnexpectedTokenSyntaxError(s.file(), s.line()+1,t, token, "'" + token + "' expected");
		}
		next(s);
	}
	
	public static String accept(String[] token, ITokenList s) throws SyntaxError {
		if (!Arrays.asList(token).contains(seek(s))) {
			String t = next(s);
			throw new UnexpectedTokenSyntaxError(s.file(), s.line()+1,t, token, "'" + token + "' expected");
		}
		return next(s);
	}
	
	
	public static String seek(String[] token, TokenScanner s) throws SyntaxError {
		if (!Arrays.asList(token).contains(seek(s))) {
			String t = seek(s);
			throw new UnexpectedTokenSyntaxError(s.file(), s.nextTokensLine()+1,t, token, "'" + token + "' expected");
		}
		return seek(s);
	}
	
	@Deprecated
	public static String seek2beta(String[] token, TokenScanner s) throws SyntaxError {
		if (!Arrays.asList(token).contains(seek2beta(s))) {
			String t = seek2beta(s);
			throw new UnexpectedTokenSyntaxError(s.file(), s.nextTokensLine()+1,t, token, "'" + token + "' expected");
		}
		return seek2beta(s);
	}
	
	public static String next(ITokenList s) throws SyntaxError {
		if (!s.hasNext()) return "<EOF>"; //throw new SyntaxError(s.line()+1, "'<TEXT>' excepted");
		return s.next();
	}
	
	public static String seek(ITokenList s) throws SyntaxError {
		if (!s.hasNext()) return "<EOF>"; //throw new SyntaxError(s.nextTokensLine()+1, "'<TEXT>' excepted");
		return s.seek();
	}
	
	@Deprecated
	public static String seek2beta(ITokenList s) throws SyntaxError {
		if (!s.hasNext()) return "<EOF>"; //throw new SyntaxError(s.nextTokensLine()+1, "'<TEXT>' excepted");
		return s.seek(1);
	}
	
	public static String seek(ITokenList s, int i) {
		if (!s.hasNext()) return "<EOF>";
		return s.seek(i);
	}
	
	public static abstract class TreeNode {

		public abstract void parse(TokenScanner s) throws SyntaxError;
		public abstract String generate(String a);
	}
}
