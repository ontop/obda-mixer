package it.unibz.inf.mixer_shell.core;

public class Template{
	private String[] splits;
	private String[] fillers;
	private String template;
	private String placeholder;
	
	public Template(String templateString){
		template = templateString;
		placeholder = "?";
		parseTemplate();
	}
	
	public Template(String templateString, String placeholder){
		template = templateString;
		this.placeholder = placeholder;
		parseTemplate();
	}
	
	/** 
	 * 
	 * @param n value greater than 1
	 * @param filler
	 */
	public void setNthPlaceholder(int n, String filler) {
		fillers[n-1] = filler;
	}
	
	private void parseTemplate(){
		splits = template.split("\\"+placeholder);
		int cnt = 0;
		for( int i = 0; i < template.length(); i++ ){
			if( template.charAt(i) == placeholder.charAt(0) ) cnt++;
		}
		fillers = new String[cnt];
	}
	public String getFilled(){
		StringBuilder temp = new StringBuilder();
		for( int i = 0; i < splits.length; i++ ){
			temp.append(splits[i]);
			if( i < fillers.length ) temp.append(fillers[i]);
		}
		return temp.toString(); 
	}
	
	public int getNumPlaceholders(){
		return fillers.length;
	}
	
	public String toString(){
		return template;
	}
};
