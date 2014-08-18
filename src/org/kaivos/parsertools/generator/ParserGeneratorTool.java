package org.kaivos.parsertools.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class ParserGeneratorTool {

	public class TreeNode {
		
		String name;
		String body = "", fields = "";
		Set<String> fields_ = new HashSet<>();
		
		public TreeNode(String name) {
			this.name = name;
		}
		
		public void addAccept(String token) {
			body += "accept(" + token + ");\n";
		}
		
		public void addNext(String var, String token) {
			if (token.startsWith("#")) {
				if (!fields_.contains(var)) fields += token.substring(1) + "Tree " + var + ";";
				body += "this." + var + " = new " + token.substring(1) + "Tree();\n";
			}
			else if (token.startsWith("+")) {
				if (!fields_.contains(var)) fields += "List<" + token.substring(1) + "Tree> " + var + " = new List<" + token.substring(1) + "Tree>;";
				body += "this." + var + ".add(new " + token.substring(1) + "Tree());\n";
			}
			else {
				if (!fields_.contains(var)) fields += "String " + var + ";";
				body += "this." + var + " = " + ((token.equals("ID")) ? "next(s)" : token) + ";\n";
			}
			
			fields_.add(var);
		}
		
		public void startLoop(String token) {
			body += "while (seek(s).equals("+token+")) {\n";
		}
		
		public void startOptional(String token) {
			body += "if (seek(s).equals("+token+")) {\n";
		}
		
		public void end() {
			body += "}\n";
		}
		
		public String getCode() {
			return genTreeNode(name , body, fields);
		}
		
	}
	
	public static String genTreeNode(String name, String body, String fields) {
		return 
	"public static class "+name+"Tree extends TreeNode {\n"+
	"		\n"+
	//"	public ArrayList<LineTree> lines = new ArrayList<LineTree>();\n"+
	//"	public ArrayList<FunctionTree> functions = new ArrayList<FunctionTree>();\n"+
	//"	\n"+
	fields+
	"\n\n"+
	"	@Override\n"+
	"	public void parse(TokenScanner s) throws SyntaxError {\n"+
			body+
	"\n	}\n"+
	"	\n"+
	"	@Override\n"+
	"	public String generate(String a) {\n"+
	"		return null;\n"+
	"	}\n"+
	"	\n"+
	"}\n";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Scanner s = new Scanner(System.in);
		ParserGeneratorTool tool = new ParserGeneratorTool();
		
		List<String> rules = new ArrayList<String>();
		rules.add("Start");
		
		for (int i = 0; i < rules.size(); i++) {
		
			String rule = rules.get(i);
			
			System.out.println("Please enter " + rule.toUpperCase() + " rule:");
			TreeNode start = tool.new TreeNode(rule);
			
			while (s.hasNext()) {
				String t = s.next();
				if (t.equals(";;;")) break;
				else if (t.equals("{")) start.startLoop(t = s.next());
				else if (t.equals("}")) start.end();
				else if (t.equals("[")) start.startOptional(t = s.next());
				else if (t.equals("]")) start.end();
				else if (t.startsWith("$")) {
					String var = t.substring(1).split("=")[0];
					t = t.substring(1).split("=")[1];
					start.addNext(var, t);
					
					if (t.startsWith("#") || t.startsWith("+")) {
						if (!rules.contains(t.substring(1))) rules.add(t.substring(1));
					}
					
					continue;
				}
				start.addAccept(t);
			}
			
			System.out.println(start.getCode());
		}
		s.close();
	}

}
