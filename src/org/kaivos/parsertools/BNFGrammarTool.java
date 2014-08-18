package org.kaivos.parsertools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BNFGrammarTool {
	
	private static class BNFRule {
		boolean separator = false;
		int separatorlevel = 0;
		
		String name;
		List<List<String>> options;
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
		
		Map<String, BNFRule> rules = new HashMap<String, BNFGrammarTool.BNFRule>();
		List<BNFRule> rulelist = new ArrayList<>();
		
		while (s.hasNext()) {
			String str = s.nextLine();
			if (str.endsWith(":") && str.length() > 1) {
				BNFRule rule = new BNFRule();
				rule.name = str.substring(0, str.length()-1);
				rule.options = new ArrayList<List<String>>();
				while (s.hasNext()) {
					String option = s.nextLine();
					if (!option.startsWith("\t")) break;
					rule.options.add(Arrays.asList(option.substring(1).split(" ")));
				}
				rules.put(rule.name, rule);
				rulelist.add(rule);
			} else if (str.startsWith("##")) {
				if (str.startsWith("## ")) {
					String text = str.substring(3);
					BNFRule rule = new BNFRule();
					rule.name = text;
					rule.separator = true;
					rule.separatorlevel = 1;
					rulelist.add(rule);
					
				} else if (str.startsWith("### ")) {
					String text = str.substring(4);
					
					BNFRule rule = new BNFRule();
					rule.name = text;
					rule.separator = true;
					rule.separatorlevel = 2;
					rulelist.add(rule);
				}
			}
		}
		
		boolean pre = true;
		
		System.out.println("<pre>");
		
		for (BNFRule rule : rulelist) {
			if (rule.separator) {
				System.out.print("</pre>");
				switch (rule.separatorlevel) {
				case 1:
					System.out.print("<hr class=\"er\"/><span>[");
					break;
				case 2:
					System.out.print("<span class=\"pieni\">[");
					break;
					
				default:
					System.out.print("<span class=\"virhe\">[");
					break;
				}
				System.out.print(rule.name + "]</span><pre>");
				pre = true;
				continue;
			}
			
			if (!pre) System.out.println();
			pre = false;
			
			System.out.println("<a id=\"rule_" + rule.name + "\">" + rule.name + "</a>:");
			for (List<String> option : rule.options) {
				System.out.print("\t");
				for (int i = 0; i < option.size(); i++) {
					if (i != 0) System.out.print(" ");
					String token = option.get(i);
					
					boolean optional = false;
					if (optional = token.endsWith(",opt")) token = token.substring(0, token.length()-4);
					
					if (Character.isUpperCase(token.charAt(0)) && !rules.containsKey(token)) {
						//System.err.println("Unknown rule " + token);
						System.out.print("" + token + "");
					}
					
					else if (Character.isUpperCase(token.charAt(0))) System.out.print("<i><a class=\"musta\" href=\"#rule_" + token + "\">" + token + "</a></i>");
					else System.out.print("<b>" + token + "</b>");
					
					if (optional) System.out.print("<sub>opt</sub>");
				}
				System.out.println();
			}
		}
		
		System.out.println("</pre>");
		
		s.close();
	}

}
