package org.kaivos.stg.error;

public class UnexpectedTokenSyntaxError extends SyntaxError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2422309261554950016L;
	
	
	private String token;
	private String excepted;
	private String[] exceptedArray;

	public UnexpectedTokenSyntaxError(String file, int line, String token, String excepted, String s) {
		super(file, line, s);
		this.setToken(token);
		this.setExcepted(excepted);
	}

	public UnexpectedTokenSyntaxError(String file, int line, String token, String[] excepted,
			String s) {
		super(file, line, s);
		this.setToken(token);
		this.setExcepted(excepted);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getExcepted() {
		return excepted;
	}

	public void setExcepted(String excepted) {
		this.excepted = excepted;
	}
	
	public void setExcepted(String[] excepted) {
		this.exceptedArray = excepted;
	}

	public String[] getExceptedArray() {
		return exceptedArray;
	}
}
