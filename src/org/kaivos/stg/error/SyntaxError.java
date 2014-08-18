package org.kaivos.stg.error;

public class SyntaxError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1308697596802939566L;
	
	
	private String message;
	private int line;
	private String file;

	public SyntaxError(String file, int line, String s) {
		this.message = s;
		this.line = line;
		this.file = file;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}
	
	public String getFile() {
		return file;
	}
	
}
