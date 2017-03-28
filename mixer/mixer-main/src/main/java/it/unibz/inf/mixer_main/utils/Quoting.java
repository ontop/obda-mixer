package it.unibz.inf.mixer_main.utils;

public enum Quoting {
    NONE("none"), 
    UNDERSCORE("underscore"), 
    PERCENT("percent");
    
    private String text;
    
    Quoting(String text) {
	this.text = text;
    }
    
    public String getText() {
	return this.text;
    }
    
    public static Quoting fromString(String text) {
	if (text != null) {
	    for (Quoting b : Quoting.values()) {
		if (text.equalsIgnoreCase(b.text)) {
		    return b;
		}
	    }
	}
	return null;
    }
};
