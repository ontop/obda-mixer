package it.unibz.inf.mixer_main.utils;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * 
 * @author tir
 *
 * Constraints: 
 * 
 * 1) The template string CANNOT start with a placeholder '?'
 * 2) There cannot be two consecutive placeholders --this would not make sense, anyway
 */
public class Template{
	private String[] splits;
	private String[] fillers;
	private String template;
	private static final String placeholder = "$";
	
	public class PlaceholderInfo{    
	    private QualifiedName qN;
	    private int id;
	    private Quoting quote; 
	    
	    private PlaceholderInfo(String s){
		// {1:blabla:PERCENT} (or NONE or UNDERSCORE)
		String tName = s.substring( s.indexOf(":") +1, s.indexOf(".") );
		String colName = s.substring(s.indexOf(".") + 1, s.lastIndexOf(":"));
		this.qN = new QualifiedName( tName, colName );
		int id = Integer.parseInt(s.substring( 1, s.indexOf(":") ));
		this.id = id;
		String quoting = s.substring(s.lastIndexOf(":") + 1, s.length() -1);
		this.quote = Quoting.fromString(quoting);
	    }
	    
	    public Quoting quote(){
		return this.quote;
	    }
	    
	    public int getId(){
		return this.id;
	    }
	    public QualifiedName getQN(){
		return this.qN;
	    }
	    
	    @Override
	    public String toString(){
		return "id = "+this.id + ", qN = " + this.qN;
	    }
	    
	    @Override 
	    public boolean equals(Object other) {
		if( this == other ) return true; // If they are the same object, then fine
		boolean result = false;
		if (other instanceof PlaceholderInfo) {
		    PlaceholderInfo that = (PlaceholderInfo) other;
		    result = this.getId() == that.getId() && this.getQN().toString().equals(that.getQN().toString());
		}
		return result;
	    }
	    
	    @Override
	    public int hashCode(){
		return this.toString().hashCode();
	    }

	    public String applyQuote(String toInsert, Quoting quotingType) {
		String result = toInsert;
		switch(quotingType){
		case NULL:
		    break;
		case PERCENT:
		    result = toInsert.replaceAll(" ", "%20");
		    break;
		case UNDERSCORE:
		    result = toInsert.replaceAll(" ", "_");
		    break;
		default:
		    break;
		}
		return result;
	    }
	};
	
	public Template(String templateString){
		template = templateString;
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
	
	/**
	 * n value greater than 1
	 * @param n
	 * @return
	 */
	public PlaceholderInfo getNthPlaceholderInfo(int n){
	    String s = splits[n];
	    return new PlaceholderInfo(s.substring( s.indexOf("{"), s.indexOf("}") + 1) );
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
		temp.append( splits[0] ); // Part before first occurrence of '$'
		if( fillers.length > 0 ){
		    temp.append(fillers[0]);
		    for( int i = 1; i < splits.length; i++ ){
			temp.append( splits[i].substring(splits[i].indexOf("}") +1, splits[i].length()) );
			if( i < fillers.length ) temp.append(fillers[i]);
		    }
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