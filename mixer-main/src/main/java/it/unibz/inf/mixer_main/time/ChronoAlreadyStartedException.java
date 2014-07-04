package it.unibz.inf.mixer_main.time;

public class ChronoAlreadyStartedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ChronoAlreadyStartedException(){
		super();
	};
	
	public ChronoAlreadyStartedException(String message){
		super(message);
	}
}