package it.unibz.inf.utils_options.ranges.exceptions;

public class NotRangeStringException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NotRangeStringException(){
		super();
	};
	
	public NotRangeStringException(String message){
		super(message);
	}
}
