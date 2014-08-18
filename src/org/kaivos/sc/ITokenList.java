package org.kaivos.sc;

public interface ITokenList {

	public boolean hasNext();
	
	public String next();
	public String seek();
	public String seek(int times);
	public int index();
	public void setIndex(int index);
	
	public int line();

	public String file();
	
}
