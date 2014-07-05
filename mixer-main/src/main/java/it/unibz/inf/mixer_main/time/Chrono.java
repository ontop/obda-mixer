package it.unibz.inf.mixer_main.time;

public class Chrono {
	private long start;
	
	public Chrono(){
		start = 0;
	}
	
	public void start(){
		if( start != 0 ){
			try{
				throw new ChronoAlreadyStartedException();
			}catch(ChronoAlreadyStartedException e){
				e.printStackTrace();
			}
		}
		start = System.currentTimeMillis();
	}
	
	public long stop(){
		long result = System.currentTimeMillis() - start;
		start = 0;
		return result;
	}
}
