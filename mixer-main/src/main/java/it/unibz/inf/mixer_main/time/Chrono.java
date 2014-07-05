package it.unibz.inf.mixer_main.time;

public class Chrono {
	private long start;
	
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
		start = 0;
		return System.currentTimeMillis() - start;
	}
}
