package org.kaivos.sc;

import java.util.ArrayList;

/**
 * Lexer class, based on delimeters
 * @author iikka
 *
 */
public class TokenScanner implements ITokenList {
	
	private ArrayList<String> lines = new ArrayList<String>();
	
	private ArrayList<Token> tokens;
	private char[] sTokens;
	private String[] bsTokens;
	private int next;
	private int line;
	public boolean parseSpaces = true;

	private boolean comments;
	private String[] commentStarts;
	private String[] commentEnds;
	
	private boolean prep;

	private String file;
	
	public TokenScanner() {
		
		next = 0;
		line = 0;
	}
	
	public void setSpecialTokens(char[] tokens) {
		this.sTokens = tokens;
	}
	
	public void setBSpecialTokens(String[] tokens) {
		this.bsTokens = tokens;
	}
	
	public void setParseSpaces(boolean b) {
		parseSpaces = b;
	}
	
	public boolean isPrep() {
		return prep;
	}

	public void setPrep(boolean prep) {
		this.prep = prep;
	}

	public boolean hasNext() {
		return next <= tokens.size()-1;
	}
	
	public boolean hasNext(int i) {
		return next+i <= tokens.size()-1;
	}
	
	public String next() {
		Token nextS = tokens.get(next++);
		/*@SuppressWarnings("unused")
		int i = 0;
		while (nextS.contains("%LNLN%") && hasNext()) {
			if (nextS.startsWith("%LNLN% ")) {
				String arg = nextS.split(" ")[1];
				int ln = Integer.parseInt(arg);
				
				line = ln-1;
			}
			line++;
			nextS = tokens.get(next++);
			i++;
		}*/
		line = nextS.line;
		file = nextS.file;
		return nextS.token;
	}
	
	public String seek() {
		Token nextS = tokens.get(next);
		/*int i = 1;
		while (nextS.contains("%LNLN%") && hasNext(i-1)) {
			nextS = tokens.get(next+i);
			i++;
		}*/
		
		return nextS.token;
	}
	
	public String seek(int times) {
		/*int i = 0;
		for (int j = 0; j < times-1; j++) {
			String nextS = tokens.get(next+i++);
			while (nextS.contains("%LNLN%") && hasNext(i-1)) {
				nextS = tokens.get(next+i);
				i++;
			}
		}
		String nextS = tokens.get(next+i++);
		while (nextS.contains("%LNLN%") && hasNext(i-1)) {
			nextS = tokens.get(next+i);
			i++;
		}*/
		Token nextS = tokens.get(next + times -1); 
		return nextS.token;
	}
	
	public int index() {
		return next;
	}
	
	public void setIndex(int i) {
		next = i;
		Token nextS = tokens.get(next);
		line = nextS.line;
		file = nextS.file;
	}
	
	public int line() {
		return line;
	}
	
	public String file() {
		return file;
	}
	
	public void setFile(String f) {
		file = f;
	}
	
	public int nextLine() {
		return next < tokens.size() ? tokens.get(next).line : line;
	}
	
	public int nextTokensLine() {
		if (!hasNext()) return line;
		/*String nextS = tokens.get(next);
		int i = 1;
		while (nextS.contains("%LNLN%") && hasNext()) {
			nextS = tokens.get(next+i);
			i++;
		}*/
		Token nextS = tokens.get(next);
		return nextS.line;
	}
	
	public String getLine() {
		return lines.get(line);
	}
	
	public String getLine(int line) {
		return lines.get(line);
	}
	
	public ArrayList<Token> getTokenList() {
		return tokens;
	}
	
	public boolean isComments() {
		return comments;
	}

	public void setComments(boolean comments) {
		this.comments = comments;
	}
	
	public void setComments(String[][] syntaxes) {
		this.comments = true;
		this.commentStarts = new String[syntaxes.length];
		this.commentEnds = new String[syntaxes.length];
		
		for (int i = 0; i < syntaxes.length; i++) {
			commentStarts[i] = syntaxes[i][0];
			commentEnds[i] = syntaxes[i][1];
		}
		
	}

	public void init(String code) throws StringIndexOutOfBoundsException {
		line = 0;
		this.tokens = parseCode(code);
		next = 0;
	}
	
	private ArrayList<Token> parseCode(String rawcode) throws StringIndexOutOfBoundsException {
		ArrayList<Token> code = new ArrayList<Token>();
		
		int char1 = 0;
		
		String text_line = "";
		
		String text = "";
		
		int curr_line = 0;
		String curr_file = file;
		
		while (char1 < rawcode.length()) {
			char char2 = rawcode.charAt(char1);
			text_line += char2;
			//System.out.print(("'"+char2+"'").replace("\n", "\\n").replace("\t", "\\t"));
			if (char1 < rawcode.length()-1 && search(bsTokens, char2, rawcode.charAt(char1+1)) > -1) {
				text_line += rawcode.charAt(char1+1);
				
				if (!text.isEmpty()) {
					code.add(new Token(text, curr_line, curr_file));
					text = "";
				}
				
				code.add(new Token("" + char2 + rawcode.charAt(char1+1), curr_line, curr_file));
				char1 += 1;
			}
			else if (search(sTokens, char2) > -1) {
				if (!text.isEmpty()) {
					code.add(new Token(text, curr_line, curr_file));
					text = "";
				}
				code.add(new Token("" + char2, curr_line, curr_file));
				
			}
			else switch (char2) {
			case '"':
				if (!text.isEmpty()) {
					code.add(new Token(text, curr_line, curr_file));
					text = "";
				}
				String text2 = "";// = rawcode.substring(char1, rawcode.indexOf('"', char1+1)+1);
				
				while(rawcode.charAt(++char1) != '\"') {
					if (rawcode.charAt(char1) == '\n') {
						curr_line++;
					}
					if (rawcode.charAt(char1) == '\\') {
						switch (rawcode.charAt(++char1)) {
						case '\"' :
							text2 += "\"";
							break;
						case 'n':
							text2 += "\n";
							break;
						case 'r':
							text2 += "\r";
							break;
						case 't':
							text2 += "\t";
							break;
						case 'b':
							text2 += "\b";
							break;
						case 'f':
							text2 += "\f";
							break;
						case '0':
							text2 += "\0";
							break;
						case '\\':
							text2 += "\\";
							break;
						}
					}
					else text2 += rawcode.charAt(char1);
				}
				
				code.add(new Token("\"" + text2 + "\"", curr_line, curr_file));
				
				//char1 = rawcode.indexOf('"', char1+1);
				break;
			case '\'':
				if (!text.isEmpty()) {
					code.add(new Token(text, curr_line, curr_file));
					text = "";
				}
				String text3 = "";// = rawcode.substring(char1, rawcode.indexOf('"', char1+1)+1);
				
				while(rawcode.charAt(++char1) != '\'') {
					if (rawcode.charAt(char1) == '\n') {
						curr_line++;
					}
					if (rawcode.charAt(char1) == '\\') {
						switch (rawcode.charAt(++char1)) {
						case '\'' :
							text3 += "'";
							break;
						case 'n':
							text3 += "\n";
							break;
						case 'r':
							text3 += "\r";
							break;
						case 't':
							text3 += "\t";
							break;
						case 'b':
							text3 += "\b";
							break;
						case 'f':
							text3 += "\f";
							break;
						case '0':
							text3 += "\0";
							break;
						case '\\':
							text3 += "\\";
							break;
						}
					}
					else text3 += rawcode.charAt(char1);
				}
				
				code.add(new Token("\'" + text3 + "\'", curr_line, curr_file));
				
				//char1 = rawcode.indexOf('"', char1+1);
				break;
			/*case '{':
				if (!text.isEmpty()) {
					code.add(text);
					text = "";
				}
				int fromS = char1+1;
				int depth = 0;
				
				while (true) {
					char char3 = rawcode.charAt(fromS++);
					if (char3 == '{') depth++;
					if (char3 == '}') depth--;
					if (depth == -1) break;
				}
				
				String text3 = rawcode.substring(char1, fromS);
				
				if (text3.contains("\n")) {
				}
				
				code.add(text3);
				
				char1 = fromS+1;
				break;*/
			case '#':
				if (comments) {
					if (!text.isEmpty()) {
						code.add(new Token(text, curr_line, curr_file));
						text = "";
					}
					String comment = "";
					while(rawcode.charAt(++char1) != '\n') {
						comment += rawcode.charAt(char1);
					}
					if (prep) {
						String[] args = comment.split(" ");
						try {
							int line = Integer.parseInt(args[1]);
							while (lines.size() < line) lines.add("");
							curr_line = line;
							curr_file = args[2].substring(1, args[2].length()-1);
						} catch (NumberFormatException ex) {
							ex.printStackTrace();
						} catch (ArrayIndexOutOfBoundsException ex) {
							ex.printStackTrace();
						}
						break;
					}
				}
				else text+=char2;
				if (!comments) break;
			case '\n':
				if (parseSpaces) {
					if (!text.isEmpty()) {
						code.add(new Token(text, curr_line, curr_file));
						text = "";
					}
				}
				curr_line++;
				{
					lines.add(text_line);
					text_line = "";
				}
				//System.out.print("LN" + code);
			case '\t':
			case ' ':
				if (parseSpaces) {
					if (!text.isEmpty()) {
						code.add(new Token(text, curr_line, curr_file));
						text = "";
					}
					break;
				}
				
			default:
				/*if (char2 == '+') {
					System.err.println("; PLUS: " + text + " CODE: " + code + " RAWCODE: " + rawcode + " SPECIAL TOKENS: " + sTokens);
				}*/
				text+=char2;
				break;
			}
			//System.out.println();
			char1++;
			//if (char2 == '~') {
			//	break;
			//}
		}
		if (!text.isEmpty()) {
			code.add(new Token(text, curr_line, curr_file));
			text = "";
		}
		lines.add(text_line);
		text_line = "";
		code.add(new Token("<EOF>", curr_line, curr_file));
		
		if (commentStarts != null)
		for (int i = 0; i < commentStarts.length; i++) {
		
			String commentStart = commentStarts[i];
			String commentEnd = commentEnds[i];
			
			boolean comment = false;
			
			for (int j = 0; j < code.size(); j++) {
				if (code.get(j).token.equals(commentStart)) {
					comment = true;
				}
				
				if (comment) {
					if (code.get(j).token.equals(commentEnd))
						comment = false;

					code.remove(j--);
				}
			}
		
		}
		
		/*for (Object o : code) {
			System.out.println(o);
		}*/
		//System.out.println(code);
		return code;
		
	}

	private int search(char[] sTokens2, char char2) {
		for (int i = 0; i < sTokens2.length; i++) {
			if (sTokens2[i] == char2) {
				return i;
			}
		}
		return -1;
	}
	
	private int search(String[] sTokens2, char char2, char char3) {
		for (int i = 0; i < sTokens2.length; i++) {
			if (sTokens2[i].charAt(0) == char2 && sTokens2[i].charAt(1) == char3) {
				return i;
			}
		}
		return -1;
	}
}
