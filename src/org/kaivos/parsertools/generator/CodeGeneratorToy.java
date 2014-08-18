package org.kaivos.parsertools.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.kaivos.stg.error.SyntaxError;
import org.kaivos.sve.interpreter.SveInterpreter;
import org.kaivos.sve.interpreter.core.SveValue;
import org.kaivos.sve.interpreter.core.SveValue.Type;
import org.kaivos.sve.interpreter.exception.SveRuntimeException;
import org.kaivos.sve.interpreter.exception.SveVariableNotFoundException;
import org.kaivos.sve.parser.SveParser;

public class CodeGeneratorToy {

	private static class BNFRule {
		String name;
		List<List<BNFToken>> options;
		List<String> codegenrule;
		String svecode;
		
		@Override
		public String toString() {
			return "BNFRule{" + options + " = " + codegenrule + "}";
		}
	}
	private static class BNFToken {
		String str;
		boolean optional;
		
		public BNFToken(String tok, boolean opt) {
			str = tok;
			optional = opt;
		}
		
		@Override
		public String toString() {
			return str;
		}
	}
	
	private static class TokenScanner {
		List<String> tokens = new ArrayList<String>();
		int i;
		
		
		public TokenScanner(Scanner in) {
			while (in.hasNext()) tokens.add(in.next());
		}
		
		public String next() {
			return tokens.get(i++);
		}
		
		public String seek() {
			return tokens.get(i);
		}
		
		public boolean hasNext() {
			return i < tokens.size();
		}
	}
	
	private static class Generator {
		Map<String, BNFRule> rules;
		TokenScanner in;
		
		SveInterpreter sve = new SveInterpreter();
		
		public Generator(Map<String, BNFRule> rules, Scanner in) {
			this.rules = rules;
			this.in = new TokenScanner(in);
		}
		
		List<String> getStartTokens(BNFRule r) {
			List<String> tokens = new ArrayList<>();
			for (List<BNFToken> tok : r.options) {
				tokens.addAll(getStartTokens(tok));
			}
			return tokens;
		}
		
		private List<String> getStartTokens(List<BNFToken> tok) {
			List<String> tokens = new ArrayList<>();
			int i = -1;
			while (tok.get(++i).optional) {
				tokens.addAll(getStartTokens(tok.get(i)));
			}
			tokens.addAll(getStartTokens(tok.get(i)));
			return tokens;
		}
		
		private List<String> getStartTokens(BNFToken token) {
			if (rules.containsKey(token.str)) return getStartTokens(rules.get(token.str));
			else return Arrays.asList(token.str);
		}
		
		SveValue generate(String rule) {
			
			String input = in.hasNext() ? in.seek() : "<<EOF>>";
			
			//System.out.println(rule + " " + input);
			
			BNFRule r = rules.get(rule);
			
			List<SveValue> parsedData = new ArrayList<>();
			
			boolean found = false;
			List<String> ptokens = new ArrayList<>();
			
			for (List<BNFToken> tok : r.options) {
				List<String> startTokens = getStartTokens(tok);
				//System.out.println(startTokens);
				if (startTokens.contains(input) || containsMatch(startTokens, input)) { 
					for (int i = 0; i < tok.size(); i++) {
						BNFToken token = tok.get(i);
						
						//System.out.print(token);
						
						input = in.hasNext() ? in.seek() : "<<EOF>>";
						
						if (rules.containsKey(token.str)) {
							List<String> startTokens1 = getStartTokens(rules.get(token.str));
							//System.out.print("RULE" + startTokens1 + input);
							if (startTokens1.contains(input) || containsMatch(startTokens1, input)) {
								parsedData.add(generate(token.str));
							}
							else if (!token.optional) error("Syntax error on token '"+input+"', one of " + startTokens1 + " expected");
							else parsedData.add(new SveValue(""));
						}
						else if (getRegex(token.str) != null) {
							if (input.matches(getRegex(token.str))) {
								parsedData.add(new SveValue(input));
								if (in.hasNext()) in.next();
							}
							else if (!token.optional) error("Syntax error on token '" + input + "', " + token.str + " expected");
							else parsedData.add(new SveValue(""));
						} else {
							if (input.equals(token.str)) {
								parsedData.add(new SveValue(input));
								if (in.hasNext()) in.next();
							}
							else if (!token.optional) error("Syntax error on token '" + input + "', " + token.str + " expected");
							else parsedData.add(new SveValue(""));
						}
					}
					//System.out.println();
					found = true;
					break;
				}
				ptokens.addAll(startTokens);
			}
			
			if (!found) {
				error("Syntax error on token '" + input + "', one of " + ptokens.toString() + " expected");
			}
			
			
			if (r.codegenrule != null) {
				String a = "";
				for (int i = 0; i < r.codegenrule.size(); i++) {
					if (i != 0) a += " ";
					try {
						a += parsedData.get(Integer.parseInt(r.codegenrule.get(i))).toString();
					} catch (NumberFormatException e) {
						a += r.codegenrule.get(i);
					}
				}
				return new SveValue(a);
			}
			else {
				SveValue p = new SveValue(Type.TABLE);
				for (int i = 0; i < parsedData.size(); i++) p.table.setLocalVar(""+i, parsedData.get(i));
				try {
					sve.globalScope.setLocalVar("@d", p);
					return sve.interpretStartTree(SveParser.parseText(r.svecode));
				} catch (SveRuntimeException
						| SyntaxError e) {
					e.printStackTrace();
					error("Incorrect Sve-code");
				}
			}
			
		}
		
		private static boolean containsMatch(Collection<?> regexes, String text) {
			for (Object o : regexes) 
				if (getRegex(o.toString()) != null)
					if (text.matches(getRegex(o.toString()))) return true;
			return false;
		}
		
		private static String getRegex(String s) {
			if (s.startsWith("/") && s.endsWith("/") && s.length() > 1) return s.substring(1, s.length()-1);
			else return null;
		}
	}
	
	/**
	 * Tulostaa virheviestin ja poistuu
	 */
	private static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner s = null;
		
		if (args.length == 0) s = new Scanner(System.in);
		else
			try {
				s = new Scanner(new File(args[0]));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		
		Map<String, BNFRule> rules = new HashMap<String, BNFRule>();
		List<BNFRule> rulelist = new ArrayList<>();
		
		while (s.hasNext()) {
			String str = s.nextLine();
			if (str.endsWith(":") && str.length() > 1) {
				BNFRule rule = new BNFRule();
				rule.name = str.substring(0, str.length()-1);
				rule.options = new ArrayList<>();
				
				String codegenrule = null;
				
				while (s.hasNext()) {
					String option = s.nextLine();
					if (!option.startsWith("\t")) {
						codegenrule = option;
						break;
					}
					List<BNFToken> tokens = new ArrayList<>();
					for (String tok : Arrays.asList(option.substring(1).split(" "))) {
						if (tok.endsWith(",opt") && tok.length() > 4) tokens.add(new BNFToken(tok.substring(0, tok.length()-4), true));
						else tokens.add(new BNFToken(tok, false));
					}
					rule.options.add(tokens);
				}
				
				if (codegenrule.startsWith("= ")) {
					rule.codegenrule = Arrays.asList(codegenrule.substring(2).split(" "));
				} 
				else if (codegenrule.startsWith("=sve ")) {
					rule.svecode = codegenrule.substring(5);
					while (s.hasNext()) {
						String l = s.nextLine();
						if (l.equals("=endsve")) break;
						rule.svecode += "\n" + l;
					}
				}
				else error("Incorrect format!");

				
				rules.put(rule.name, rule);
				rulelist.add(rule);
				
			}
			else if (str.equals("### EOF")) break;
		}
		
		System.out.println(rules);
		
		System.out.println(new Generator(rules, new Scanner(System.in)).generate("Start"));
	}

}
