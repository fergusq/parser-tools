package org.kaivos.parsertools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaivos.sc.ITokenList;

public class ParserGenerationTools {

	public final class Rule extends ParserRule {
		
		private Object[][] rule;
		private String name;
		
		public Rule(Object[][] rule) {
			this.rule = rule;
		}
		
		public Rule(String name, Object[][] rule) {
			this.rule = rule;
			this.name = name;
		}
		
		@Override
		public Object[][] getRule() {
			return rule;
		}
		
	}
	
	public final class Rule_Optional extends ParserRule {
		
		private Object[][] rule;
		
		public Rule_Optional(Object[][] rule) {
			this.rule = rule;
		}
		
		@Override
		public Object[][] getRule() {
			return rule;
		}
		
	}
	
	public final class Rule_Infinite extends ParserRule {
		
		private Object[][] rule;
		
		public Rule_Infinite(Object[][] rule) {
			this.rule = rule;
		}
		
		@Override
		public Object[][] getRule() {
			return rule;
		}
		
	}
	
	public final class RuleReference<T extends ParserRule> {
		
		private Class<T> ref;
		
		public RuleReference(Class<T> cl) {
			ref = cl;
		}
		
		public Class<T> getRef() {
			return ref;
		}
		
		public T getInstance() throws InstantiationException, IllegalAccessException {
			return ref.newInstance();
		}
		
	}
	
	public final class RuleReferenceString {
		
		private String ref;
		
		public RuleReferenceString(String cl) {
			ref = cl;
		}
		
		public String getRef() {
			return ref;
		}
		
		public ParserRule getInstance() {
			return ruleMap.get(ref);
		}
		
	}
	
	public final class RuleProperty {
		
		private Object rule;
		private String name;
		private boolean list = false;
		
		public RuleProperty(String name, Object rule) {
			this.name = name;
			this.rule = rule;
		}
		
		public RuleProperty(String name, boolean list, Object rule) {
			this.name = name;
			this.rule = rule;
			this.list = list;
		}
		
		public Object getRule() {
			return rule;
		}
		
	}
	
	public enum SpecialToken {
		IDENTIFIER,
		NUMBER,
		STRING
		
	}
	
	public class SemanticSelector {
		public String selector;

		public SemanticSelector(String selector) {
			super();
			this.selector = selector;
		}
	}
	
	public class ASTProcessorFunction {
		public String f;

		public ASTProcessorFunction(String selector) {
			super();
			this.f = selector;
		}
	}
	
	public abstract class ParserRule {
	
		public abstract Object[][] getRule();
		
		public Set<String> getFirstToken() throws CodeGenerationException {
			
			Set<String> ft = new HashSet<>();
			
			for (int i = 0; i < getRule().length; i++) {
				Object[] rule = getRule()[i];
			
				ft.addAll(getStartToken(rule));
			}
			
			return ft;
		}
		
		public Collection<? extends String> getStartToken(Object[] rule) throws CodeGenerationException {
			if (rule[0] instanceof Rule_Optional){
				Set<String> set = new HashSet<>();
				for (int i = 0; i < rule.length && (i > 0 ? (rule[i-1] instanceof Rule_Optional|rule[i-1] instanceof Rule_Infinite) : true); i++) {
					set.addAll(getStartToken_b(rule[i]));
				}
				return ( set );
			}
			
			else if (rule[0] instanceof Rule_Infinite){
				Set<String> set = new HashSet<>();
				for (int i = 0; i < rule.length && (i > 0 ? (rule[i-1] instanceof Rule_Optional|rule[i-1] instanceof Rule_Infinite) : true); i++) {
					set.addAll(getStartToken_b(rule[i]));
				}
				return ( set );
			}
			
			else return getStartToken_b(rule[0]);
		}
		
		public Collection<? extends String> getStartToken_b(Object rule) throws CodeGenerationException {
			if (rule instanceof String) return ( Arrays.asList((String) rule));
			if (rule instanceof Object[][]) return ( new Rule((Object[][]) rule).getFirstToken() );
			
			if (rule instanceof ParserRule) return ( ((ParserRule) rule).getFirstToken() );
			if (rule instanceof RuleProperty) return getStartToken_b(((RuleProperty) rule).rule);
			if (rule instanceof RuleReferenceString) {
				if (((RuleReferenceString) rule).getInstance() == null) 
					throw new CodeGenerationException("Invalid rule: " + ((RuleReferenceString) rule).ref + " not found");
				return ((RuleReferenceString) rule).getInstance().getFirstToken();
			}
			if (rule instanceof RuleReference<?>);
			if (rule instanceof SpecialToken) {
				if (rule == SpecialToken.IDENTIFIER) {
					return Arrays.asList("<ID>");
				}
				else if (rule == SpecialToken.NUMBER) {
					return Arrays.asList("<NUMBER>");
				}
				else if (rule == SpecialToken.STRING) {
					return Arrays.asList("<STRING>");
				}
			}
			if (rule instanceof SemanticSelector) {
				return Arrays.asList("<" + ((SemanticSelector) rule).selector + ">");
			}
			if (rule instanceof ASTProcessorFunction) {
				throw new CodeGenerationException("Invalid rule: ASTProcessorFunction can't start rule");
			}
			
			return new HashSet<>();
		}
	
		public boolean match(ITokenList list) throws CodeGenerationException {
			
			boolean matched = false;
			
			String start = list.seek();
			int si = list.index(), ei = si;
			
			for (int i = 0; i < getRule().length; i++) {
				Object[] rule = getRule()[i];
			
				if (getStartToken(rule).contains(start)) {
					matched = matched || matchList(rule, list);
				}
				if (list.index() > ei) ei = list.index();
				list.setIndex(si);
			}
			
			list.setIndex(ei);
			return matched;
		}
	
		private boolean matchList(Object[] rule, ITokenList list) throws CodeGenerationException {
			for (int i = 0; i < rule.length; i++) {
				if (rule[i] instanceof String) if (! ((String) rule[i]).equals(list.next()) ) return false;
				if (rule[i] instanceof Object[][]) if (! new Rule((Object[][]) rule[i]).match(list) ) return false;
				
				if (rule[i] instanceof Rule_Optional) {
					int si = list.index();
					if (! ((ParserRule) rule[i]).match(list) ) list.setIndex(si);
				}
				
				else if (rule[i] instanceof Rule_Infinite) {
					int si = list.index();
					while (((ParserRule) rule[i]).match(list) ) {
						si = list.index();
					}
					list.setIndex(si);
				}
				
				else if (rule[i] instanceof ParserRule) if (! ((ParserRule) rule[i]).match(list) ) return false;
				
				if (rule[i] instanceof RuleProperty) if (! matchList(new Object[] {((RuleProperty) rule[i]).rule}, list)) return false;
				if (rule[i] instanceof RuleReferenceString) {
					if (! ((RuleReferenceString) rule[i]).getInstance().match(list) ) return false;
				}
				if (rule[i] instanceof RuleReference<?>)
					try {
						if (! ((RuleReference<?>) rule[i]).getInstance().match(list) ) return false;
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					};
				if (rule[i] instanceof SpecialToken) {
					switch ((SpecialToken) rule[i]) {
					case IDENTIFIER:
						break;
					case NUMBER:
						try {  Integer.parseInt(list.next()); } catch (NumberFormatException ex) { return false; }
						break;
					case STRING:
						String s = list.next();
						if (!s.startsWith("\"") || !s.endsWith("\"")) return false;
						break;
					default:
						break;
					}
				}
			}
			return true;	
		}
		
		public String createParser(boolean matcherOnly) throws CodeGenerationException {
			
			String parser = "";
			
			if (!LL1) {
			
				parser += s("String s2 = seek(s);\n");
				parser += s("int si = s.index(), ei = si;\n");
				parser += s("int longest = -1;\n");
				
				for (int i = 0; i < getRule().length; i++) {
					Object[] rule = getRule()[i];
				
					// Tarkista onko seuraava token mahdollinen
					parser += s("r" + i + ": if (");
					Collection<? extends String> c = getStartToken(rule);
					Iterator<? extends String> it = c.iterator();
					for (int j = 0; j < c.size(); j++) {
						if (j != 0) parser += " || ";
						
						String tok = it.next();
						
						if (tok.equals("<ID>")) parser += "Parser.isValidIdentifier(s2)";
						else if (tok.equals("<NUMBER>")) parser += "s2.matches(\"[0-9]+\")";
						else if (tok.equals("<STRING>")) parser += "s2.startsWith(\"\\\"\")";
						else if (tok.equals("<EOF>")) parser += "s2.equals(\"<EOF>\")";
						// Ennalta määritelty java-funktio tarkistaa tokenin 
						else if (tok.matches("<([a-zA-Z_][a-zA-Z0-9_]*)(\\.[a-zA-Z_][a-zA-Z0-9_]*)*>")) 
							parser += "" + tok.substring(1, tok.length()-1) + "(s2)";
						
						else parser += "s2.equals(\"" + tok + "\")";
					}
					parser += ") {\n";
					
					tabs++; // korota sisennystä
					
					parser += s("if (longest == -1) longest = " + i + ";\n");
					parser += createParser_List(rule, true, false, i);
					
					parser += s("if (s.index() > ei) {\nlongest = " + i + ";\nei = s.index();\n}\n");
					
					tabs--; // vähennä sisennystä
					parser += s("}\n");
					parser += s("s.setIndex(si);\n");
				}
				
				//parser += "if (longest == -1) longest = 0;\n";
				
				//if (matched) parser += createParser_List(getRule()[longest], list);
				
				for (int i = 0; i < getRule().length; i++) {
					Object[] rule = getRule()[i];
		
					if (i != 0) parser += s("else ");
					else parser += s(""); // tulosta sisennykset
					
					parser += "if (longest == "+i+") {\n";
					tabs++; // sisennys ++
					
					parser += createParser_List(rule, false, !matcherOnly, -1);
					
					tabs--; // sisennys --
					parser += s("}\n");
				}
				
				parser += s("else return false;\n");
				parser += s("return true;\n");
			} else {
				parser += s("String s2 = seek(s);\n");
				
				Map<String, ArrayList<Integer>> alreadyChecked = new HashMap<>();
				
				String n = "new String[] {";
				List<String> list = new ArrayList<String>();
				
				for (int i = 0; i < getRule().length; i++) {
					Object[] rule = getRule()[i];
					
					// Tarkista onko seuraava token mahdollinen
					parser += s("if (");
					Collection<? extends String> c = getStartToken(rule);
					Iterator<? extends String> it = c.iterator();
					for (int j = 0; j < c.size(); j++) {
						if (j != 0) parser += " || ";
						
						String tok = it.next();
						if (alreadyChecked.containsKey(tok)) {
							// tokeni on kahteen kertaan, kielioppi ei ole LL(1)
							alreadyChecked.get(tok).add(i);
						}
						else alreadyChecked.put(tok, new ArrayList<>(Arrays.asList(i)));
						
						if (tok.equals("<ID>")) parser += "Parser.isValidIdentifier(s2)";
						else if (tok.equals("<NUMBER>")) parser += "s2.matches(\"[0-9]+\")";
						else if (tok.equals("<STRING>")) parser += "s2.startsWith(\"\\\"\")";
						else if (tok.equals("<EOF>")) parser += "s2.equals(\"<EOF>\")";
						// Ennalta määritelty java-funktio tarkistaa tokenin 
						else if (tok.matches("<([a-zA-Z_][a-zA-Z0-9_]*)(\\.[a-zA-Z_][a-zA-Z0-9_]*)*>")) 
							parser += "" + tok.substring(1, tok.length()-1) + "(s2)";
						
						else parser += "s2.equals(\"" + tok + "\")";
						
						list.add(tok);
					}
					parser += ") {\n";
					tabs++; // lisää sisennystä
					
					parser += createParser_List(rule, false, true, i);
					parser += s("return true;\n");
					
					tabs--; // vähennä sisennystä
					parser += s("}\n");
				}
				
				if (!matcherOnly) {
					Collections.sort(list);
					for (int i = 0; i < list.size(); i++) n += (i != 0 ? ", " : "") + "\"" + list.get(i) + "\"";
					parser += s("accept("+n+"}, s);\n");
				}
				parser += s("return false;\n");
				
				// tulosta varoitukset
				for (Entry<String, ArrayList<Integer>> a : alreadyChecked.entrySet()) {
					if (a.getValue().size() > 1)
						System.err.println("(" + currentRule + ") Conflict: '" + a.getKey() + "' can match multiple alternatives " + a.getValue() + 
								", disabled alternatives " + a.getValue().subList(1, a.getValue().size())); 
				}
			}
			return parser;
		}
	
		private String checkFirstTokens(Object rule, String next) throws CodeGenerationException {
			String parser = s("if (");
			Collection<? extends String> c = (((ParserRule) rule).getFirstToken());
			Iterator<? extends String> it = c.iterator();
			for (int j = 0; j < c.size(); j++) {
				if (j != 0) parser += " || ";
				
				String tok = it.next();
				
				if (tok.equals("<ID>")) parser += "Parser.isValidIdentifier("+next+")";
				else if (tok.equals("<NUMBER>")) parser += ""+next+".matches(\"[0-9]+\")";
				else if (tok.equals("<STRING>")) parser += ""+next+".startsWith(\"\\\"\")";
				else if (tok.equals("<EOF>")) parser += ""+next+".equals(\"<EOF>\")";
				// Ennalta määritelty java-funktio tarkistaa tokenin 
				else if (tok.matches("<([a-zA-Z_][a-zA-Z0-9_]*)(\\.[a-zA-Z_][a-zA-Z0-9_]*)*>")) 
					parser += "" + tok.substring(1, tok.length()-1) + "("+next+")";
				
				else parser += ""+next+".equals(\"" + tok + "\")";
			}
			parser += ") {\n"; tabs++;
			return parser;
		}
		
		private String createParser_List(Object[] rule, boolean matcher, boolean comments, int id) throws CodeGenerationException {
			String parser = "";
			for (int i = 0; i < rule.length; i++) {
				if (rule[i] instanceof String) {
					if (matcher) {
						parser += s("if (!next(s).equals(\"" + (String) rule[i] + "\")) break r"+id+";\n");
					}
					else parser += s("accept(\"" + (String) rule[i] + "\", s);\n");
					
					possibleKeywords.add((String) rule[i]);
				}
				if (rule[i] instanceof Object[][]) {
					parser += s("if (!new ParserTreeRule() {\n"); tabs++;
					parser += s(
							"@Override\n");
					parser += s("public boolean parse(ITokenList s) throws SyntaxError {\n" +
							new Rule((Object[][]) rule[i]).createParser(matcher || !comments) + 
							s("}\n"));
					tabs--;
					parser += s("}.parse(s))");
					if (matcher) parser += (" break r"+id+";\n");
					else parser += (";\n");
				}
				
				if (rule[i] instanceof Rule_Optional) {
					parser += checkFirstTokens(rule[i], "seek(s)");
					parser += s("int si2 = s.index();\n");
					parser += s("if (!new ParserTreeRule() {\n"); tabs++;
					parser += s(
							"@Override\n");
					parser += s("public boolean parse(ITokenList s) throws SyntaxError {\n" +
							((ParserRule) rule[i]).createParser(matcher || !comments) + 
							s("}\n"));
					tabs--;
					parser += s("}.parse(s)) s.setIndex(si2);\n");
					tabs--;
					parser += s("}\n");
				}
				
				else if (rule[i] instanceof Rule_Infinite) {
					parser += checkFirstTokens(rule[i], "seek(s)");
					parser += s("int si2 = s.index();\n");
					parser += s("while (new ParserTreeRule() {\n"); tabs++;
					parser += s(
							"@Override\n");
					parser += s("public boolean parse(ITokenList s) throws SyntaxError {\n" +
							checkFirstTokens(rule[i], "seek(s)") +
							((ParserRule) rule[i]).createParser(matcher || !comments) + eat(tabs--) +
							s("} return false;\n") + s("}\n"));
					tabs--;
					parser += s("}.parse(s)) si2 = s.index();\n");
					parser += s("s.setIndex(si2);\n");
					tabs--;
					parser += s("}\n");
				}
				
				else
				
				if (rule[i] instanceof ParserRule) {
					parser += s("{\n"); tabs++;
					parser += s("try {\n"); tabs++;
					parser += s("ParserTreeRule rule = new " +
							(rule[i] instanceof Rule && !matcher ? 
									createNewRule(((Rule) rule[i]).name, ((ParserRule) rule[i]).createParser(matcher)) : 
									createNewRule(((ParserRule) rule[i]).createParser(matcher)))+
							"();\n");
					parser += s("if (!rule.parse(s)) ");
					if (matcher) parser += "break r"+id+";\n";
					//else if (comments) parser += "; else children.add(rule);\n";
					else parser += ";\n";
					tabs--;
					parser += s("} catch (SyntaxError ex) {"); tabs--;
					if (matcher) parser += s("break r"+id+";\n");
					else parser += s(";\n");
					tabs--; parser += s("}\n");
					tabs--; parser += s("}\n");
				}
				if (rule[i] instanceof RuleReferenceString)
					{
						parser += s("{\n"); tabs++;
						parser += s("try {\n"); tabs++;
						parser += s("ParserTreeRule rule = new GeneratedRule_" +
								((RuleReferenceString) rule[i]).ref + "" + 
								"();\n");
						parser += s("if (!rule.parse(s)) ");
						if (matcher) parser += "break r"+id+";\n";
						//else if (comments) parser += "; else children.add(rule);\n";
						else parser += "\n;";
						tabs--; parser += s("} catch (SyntaxError ex) {"); tabs++;
						if (matcher) parser += s("break r"+id+";\n");
						else parser += s(";\n");
						tabs--; parser += s("}\n");
						tabs--; parser += s("}\n");
					}
			
				if (rule[i] instanceof RuleReference<?>)
					try {
						parser += s("{\n"); tabs++;
						parser += s("ParserTreeRule rule = new " +
								createNewRule(
										"/* "+((RuleReference<?>) rule[i]).getClass().getSimpleName()+" */\n" + 
										((RuleReference<?>) rule[i]).getInstance().createParser(matcher))+
								"();\n");
						parser += s("if (!rule.parse(s)) ");
						if (matcher) parser += "break r"+id+";\n";
						//else if (comments) parser += "; else children.add(rule);\n";
						else parser += ";\n";
						tabs--; parser += s("}\n");
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					};
				if (rule[i] instanceof RuleProperty) {
					Object a = ((RuleProperty) rule[i]).rule;
					String name = "\"" + ((RuleProperty) rule[i]).name + "\"";
					currentProperties.put(name, (RuleProperty) rule[i]);
					boolean list = ((RuleProperty) rule[i]).list;
					if (a instanceof String) {
						if (matcher) {
							parser += s("if (!next(s).equals(\"" + (String) a + "\")) break r"+id+";\n");
						}
						else if (comments) {
							parser += s("if (seek(s).equals(\"" + (String) a + "\")) "+putProperty(name, "next(s)", list)+";\n");
							parser += s("else accept(\"" + (String) a + "\", s);\n");
						} else {
							parser += s("accept(\"" + (String) a + "\", s);\n");
						}
						possibleKeywords.add((String) a);
					} else if (a instanceof SpecialToken) {
						if (!matcher && comments)
							parser += s(""+putProperty(name, "next(s)", list)+";\n");
						else {
							switch ((SpecialToken) a) {
							case IDENTIFIER:
								if (matcher) {
									parser += s("if (!isValidIdentifier(next(s))) break r"+id+";\n");
								}
								else {
									parser += s("next(s);\n");
								}
								break;
							case NUMBER:
								{
									parser += s("next(s);\n"); // TODO Tarkista onko numero
								}
								break;
							case STRING:
								if (matcher) {
									parser += s("if (!seek(s).startsWith(\"\\\"\") || !next(s).endsWith(\"\\\"\")) break r"+id+";\n");
								}
								else {
									parser += s("next(s);\n");
								}
								break;
							default:
								break;
							}
						}
					} else if (a instanceof ParserRule) {
						if (matcher) {
							parser += s("try {\n"); tabs++; // try vain jos matcheri!
						}
						parser += s("{\n"); tabs++;
						parser += s("ParserTreeRule rule = new " +  
								(a instanceof Rule && !matcher ? 
										createNewRule(((Rule) a).name, ((ParserRule) a).createParser(matcher)) : 
										createNewRule(((ParserRule) a).createParser(matcher)))+
								"();\n");
						parser += s("if (!rule.parse(s)) ");
						if (matcher) parser += "break r"+id+";\n";
						else if (comments) parser += "; else "+putProperty(name, "rule", list)+";\n";
						else parser += ";\n";
						if (matcher) {
							tabs--;
							parser += s("} catch (SyntaxError ex) {"); tabs++;
							parser += s("break r"+id+";\n");
							tabs--;
							parser += s("}\n");
						}
						tabs--;
						parser += s("}\n");
					} else if (a instanceof RuleReferenceString)
					{
						if (matcher) {
							parser += s("try {\n"); tabs++; // try vain jos matcheri!
						}
						parser += s("{\n"); tabs++;
						parser += s("ParserTreeRule rule = new GeneratedRule_" +
								((RuleReferenceString) a).ref + "" + 
								"();\n");
						parser += s("if(!rule.parse(s)) ");
						if (matcher) parser += "break r"+id+";\n";
						else if (comments) parser += "; else "+putProperty(name, "rule", list)+";\n";
						else parser += ";\n";
						if (matcher) {
							tabs--;
							parser += s("} catch (SyntaxError ex) {"); tabs++;
							parser += s("break r"+id+";\n");
							tabs--;
							parser += s("}\n");
						}
						tabs--;
						parser += s("}\n");
						//parser += 			"System.out.println(\">\" + s.index() + \" \" + s.seek());\n";
					} else throw new RuntimeException("(" + Arrays.toString(rule) + ") IERR_NOT_IMPLEMENTED(" + a.getClass().getSimpleName() + ")");
				}
				if (rule[i] instanceof SpecialToken) {
					switch ((SpecialToken) rule[i]) {
					case IDENTIFIER:
						if (matcher) {
							parser += s("if (!isValidIdentifier(next(s))) break r"+id+";\n");
						}
						else {
							parser += s("next(s);\n");
						}
						break;
					case NUMBER:
						{
							parser += s("next(s);\n"); // TODO Tarkista onko numero
						}
						break;
					case STRING:
						if (matcher) {
							parser += s("if (!seek(s).startsWith(\"\\\"\") || !next(s).endsWith(\"\\\"\")) break r"+id+";\n");
						}
						else {
							parser += s("next(s);\n");
						}
						break;
					default:
						break;
					}
				}
				
				if (rule[i] instanceof SemanticSelector) {
					parser += "// Custom selector " + ((SemanticSelector) rule[i]).selector + " has been applied before, no action needed\n";
				}
				if (rule[i] instanceof ASTProcessorFunction) {
					if (!matcher && comments)
						parser += s("" + ((ASTProcessorFunction) rule[i]).f + "(ast_"+currentRule+"(GeneratedRule_"+currentRule+".this));\n");
				}
			}
			return parser;	
		
		}
		
		private String putProperty(String name, String value, boolean list) {
			if (!list) {
				return "children.put(" + name + ", " + value + ")";
			} else {
				return ("if (children.get(" + name + ") == null) {\n") + eat(tabs++) + 
						s("children.put(" + name + ", new ArrayList<Object>());\n") +
						s("((List<Object>) children.get(" + name + ")).add(" + value + ");\n") + eat (tabs--) + s("}\n") + s("else ") +
						("((List<Object>) children.get(" + name + ")).add(" + value + ")");
			}
		}
	}
	
	static int tabs = 3;
	
	/**
	 * Sisentää tekstin
	 * 
	 * Lisää tekstin eteen \t-merkkejä tabs-kentän mukaan
	 * @param s Muunneltava teksti
	 * @return Sisennetty teksti
	 */
	private static String s(String s) {
		for (int i = 0; i < tabs; i++) s = "\t" + s;
		return s;
	}
	
	private static String eat(Object o) {
		return "";
	}
	
	public boolean LL1 = false;
	
	public int ruleCounter = 0;
	public String ruleClasses = new String(), codeGenClass = new String();
	private HashMap<String, Set<String>> astClasses = new HashMap<>();
	private HashMap<String, List<String>> codeGenFunctions = new HashMap<>();
	private HashMap<String, String> rules = new HashMap<>();
	private HashMap<String, ParserRule> ruleMap = new HashMap<>();
	private HashMap<String, String> aliases = new HashMap<>();
	
	public String currentRule = "";
	private HashMap<String, RuleProperty> currentProperties = new HashMap<>();
	public HashSet<String> possibleKeywords = new HashSet<>();
	
	public void addRule(String name, Rule rule) {
		addRule(name, name, rule);
	}
	
	public void addRule(String name, String container, Rule rule) {
		ruleMap.put(name, rule);
		aliases.put(name, container);
	}
	
	/**
	 * Get Container
	 * @param rule Rule
	 * @return Name of container
	 */
	public String gC(String rule) {
		return aliases.get(rule);
	}
	
	public String parser(ParserRule rule) throws CodeGenerationException {
		ruleClasses = codeGenClass = ""; // reset data
		ruleCounter = 0;
		rules.clear();
		
		for (Entry<String, ParserRule> r : ruleMap.entrySet()) 
		{
			currentProperties.clear();
			currentRule = r.getKey();
			createNewRule(r.getKey(), r.getValue().createParser(false));
			
			createASTFunction(r.getKey());
			createASTClass(r.getKey());
			
			createCodeGenFunction(r.getKey());
		}
		
		createNewRule(rule.createParser(false));
		
		for (Entry<String, Set<String>> b : astClasses.entrySet()) {
			ruleClasses += "\tpublic class Rule_" + gC(b.getKey()) + " {\n";
			for (String field : b.getValue()) {
				ruleClasses += field;
			}
			ruleClasses += "\t}\n\n";
		}
		
		for (Entry<String, List<String>> b : codeGenFunctions.entrySet()) {
			codeGenClass += "\tpublic static String generate_" + gC(b.getKey()) + "(Parser.Rule_" + gC(b.getKey()) + " rule) {\n";
			codeGenClass += "\t\tif (rule == null) return \"\";\n";
			for (String field : b.getValue()) {
				codeGenClass += field;
			}
			codeGenClass += "\t}\n\n";
		}
		
		return ruleClasses;
	}
	
	private void createASTClass(String key) {
		// AST-luokka
		Set<String> astClass;
		if (astClasses.get(gC(key)) == null) {
			astClasses.put(gC(key), astClass = new HashSet<String>());
			
		} else astClass = astClasses.get(gC(key));
		
		for (RuleProperty p : currentProperties.values()) {
			String type = "Object";
			if (p.rule instanceof String) {
				type = "String";
			} else if (p.rule instanceof RuleReferenceString) {
				type = "Rule_" + gC(((RuleReferenceString) p.rule).ref);
			} else if (p.rule instanceof SpecialToken) {
				if (((SpecialToken) p.rule) == SpecialToken.NUMBER)
					type = "int";
				else
					type = "String";
			}
			if (p.list) type = "List<" + type + ">";
			
			astClass.add("\t\tpublic " + type + " " + p.name + ";\n");
		}
		
	}
	
	private void createCodeGenFunction(String key) {
		// AST-luokka
		List<String> cgf;
		if (codeGenFunctions.get(gC(key)) == null) {
			codeGenFunctions.put(gC(key), cgf = new ArrayList<String>());
			
		} else cgf = codeGenFunctions.get(gC(key));
		
		cgf.add("\t\tString a = \"\";\n");
		
		for (RuleProperty p : currentProperties.values()) {
			if (p.list) {
				if (p.rule instanceof String) {
					cgf.add("\t\tif (rule." + p.name + " != null) { a += \"" + p.name + " = [\"; for (String s : rule." + p.name + ") a += (s + \",\"); a+=\"]\"; }\n");
				} else if (p.rule instanceof SpecialToken) {
					cgf.add("\t\tif (rule." + p.name + " != null) { a += \"" + p.name + " = [\"; for (Object s : rule." + p.name + ") a += (s + \",\"); a+=\"]\"; }\n");
				} else if (p.rule instanceof RuleReferenceString) {
					cgf.add("\t\tif (rule." + p.name + " != null) { a += \"" + p.name + " = [\"; for (Parser.Rule_" + gC(((RuleReferenceString) p.rule).ref) + " s : rule." + p.name + ") " +
							"a += generate_" + gC(((RuleReferenceString) p.rule).ref) + "(s) + \",\"; a+=\"]\"; }\n");
				} 
			}
			
			else if (p.rule instanceof String) {
				cgf.add("\t\tif (rule." + p.name + " != null) a += (\"" + p.name + " = \" + rule." + p.name + " + \",\");\n");
			} else if (p.rule instanceof SpecialToken) {
				if (((SpecialToken)p.rule) == SpecialToken.NUMBER)
					cgf.add("\t\ta += (\"" + p.name + " = \" + rule." + p.name + " + \",\");\n");
				else cgf.add("\t\tif (rule." + p.name + " != null) a += (\"" + p.name + " = \" + rule." + p.name + " + \",\");\n");
			} else if (p.rule instanceof RuleReferenceString) {
				cgf.add("\t\tif (rule." + p.name + " != null) a += \"" + p.name + " = \" + generate_" + gC(((RuleReferenceString) p.rule).ref) + "(rule." + p.name + ") + \",\";\n");
			} 
		}
		
		cgf.add("\t\treturn \"{\" + a + \"}\";\n");
		
	}
	
	private void createASTFunction(String key) {
		// funktio joka rakentaa ASTin
		ruleClasses += "\t@SuppressWarnings(\"all\")\n";
		ruleClasses += "\tprivate Rule_" + gC(key) + " ast_" + key + "(PTreeNode node) {\n";
		ruleClasses += "\t\tif (node == null) return null;\n";
		ruleClasses += "\t\tRule_" + gC(key) + " a = new Rule_" + gC(key) + "();\n";
		for (RuleProperty p : currentProperties.values()) {
			
			String type = "Object";
			if (p.rule instanceof String) {
				type = "String";
			} else if (p.rule instanceof RuleReferenceString) {
				type = "Rule_" + gC(((RuleReferenceString) p.rule).ref);
			} else if (p.rule instanceof SpecialToken) {
				if (((SpecialToken) p.rule) == SpecialToken.NUMBER)
					type = "int";
				else
					type = "String";
			}
			if (p.list) type = "List<" + type + ">";
			
			if (p.rule instanceof RuleReferenceString && !p.list) { // Jos kyseessä ei ole lista, muunna solu suoraan
				ruleClasses += "\t\ta." + p.name + " = ";
				ruleClasses += "ast_" + ((RuleReferenceString) p.rule).ref + 
						"((PTreeNode) node.children.get(\"" + p.name + "\"));\n";
			} else if (p.rule instanceof RuleReferenceString) { 
				// jos kyseessä on lista, käy läpi jokainen listan solu 
				// ja rakenna yksitellen jokaisen AST, laita tulos uuteen tauluun
				ruleClasses += "\t\ta." + p.name + " = new ArrayList<>();\n";
				ruleClasses += "\t\tif (node.children.get(\"" + p.name + "\") != null)\n";
				ruleClasses += "\t\tfor (Object o: (List<?>) node.children.get(\"" + p.name + "\")) {\n";
				ruleClasses += "\t\t\ta." + p.name + ".add(";
				ruleClasses += "ast_" + ((RuleReferenceString) p.rule).ref + 
						"((PTreeNode) o));\n";
				ruleClasses += "\t\t}\n";
			} else {
				// Jos kyseessä ei ole AST-solu vaan yksittäinen merkkijono
				ruleClasses += "\t\tif (node.children.get(\"" + p.name + "\") != null)\n";
				ruleClasses += "\t\ta." + p.name + " = ";
				if (p.rule instanceof SpecialToken && ((SpecialToken) p.rule) == SpecialToken.NUMBER) // Jos solu on NUMBER, muunna se stringistä intiksi
					ruleClasses += "Integer.parseInt((String) node.children.get(\"" + p.name + "\"));\n";
				else ruleClasses += "((" + type + ") node.children.get(\"" + p.name + "\"));\n";
			}
		}
		ruleClasses += "\t\treturn a;\n";
		ruleClasses += "\t}\n\n";
	}

	private String createNewRule(String body) {
		if (rules.containsKey(""+body.hashCode())) {
			return "GeneratedRule_" + rules.get(""+body.hashCode());
		}
		
		rules.put(body.hashCode()+"", ""+ruleCounter);
		
		String name = "GeneratedRule_" + ruleCounter++;
		ruleClasses += "	class " + name + " extends PTreeNode implements ParserTreeRule {\n" +
			
			"		@SuppressWarnings(\"all\")\n" +
			"		@Override\n" +
			"		public boolean parse(ITokenList s) throws SyntaxError {\n" +
						body + "\n" +
			"		}\n" +
			"		@Override\n" +
			"		public String toString() {\n" +
			"			return children.toString();\n" + 
			"		}\n" +
			"	}\n\n";
		return name;
	}
	
	private String createNewRule(String name1, String body) {
		if (rules.containsKey(""+body.hashCode())) {
			return "GeneratedRule_" + rules.get(""+body.hashCode());
		}
		
		rules.put(body.hashCode()+"", name1);
		
		String name = "GeneratedRule_" + name1;
		ruleClasses += "	class " + name + " extends PTreeNode implements ParserTreeRule {\n" +
			"		@SuppressWarnings(\"all\")\n" +
			"		@Override\n" +
			"		public boolean parse(ITokenList s) throws SyntaxError {\n" +
						body + "\n" +
			"		}\n" +
			"		@Override\n" +
			"		public String toString() {\n" +
			"			return children.toString();\n" + 
			"		}\n" +
			"	}\n\n";
		return name;
	}
	
	
}
