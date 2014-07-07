package it.unibz.inf.mixer_main.statistics;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
	
	// SET THIS TO TRUE IF YOU WANT TO ENABLE STATISTICS
	private final boolean active = true;
	
	private Map<String, SimpleStatistics> mStats = new HashMap<String, SimpleStatistics>();
	private String curLabel;
		
	public Statistics(String curLabel){
		this.curLabel = curLabel;
	}
	
	
	public void setLabel(String label){
		if( !active ) return; 
		
		curLabel = label;
	}
	
	public String getLabel(){
		return curLabel;
	}
	
	public SimpleStatistics getSimpleStatsInstance(String label){
		if( !active ) return null; 
		
		SimpleStatistics result = null;
		
		if( mStats.containsKey(label) ){
			result = mStats.get(label);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			mStats.put(label, stat);
			result = stat;
		}
		return result;
	}
		
	public String printStats(){
		
		StringBuilder result = new StringBuilder();
		
		for( String label : mStats.keySet() ){
			result.append(mStats.get(label).printStats());
		}
		
		return result.toString();
	}
	
	public void reset(){
		mStats.clear();
		System.gc();
	}	
	public synchronized void merge(Statistics toMerge){
		
		for( String label : toMerge.mStats.keySet() ){
			if( mStats.containsKey(label) ){
				try{
					throw new StatisticsUnmergeableException();
				}catch(StatisticsUnmergeableException e){
					e.printStackTrace();
				}	
			}
			else{
				mStats.put(label, toMerge.mStats.get(label));
			}
		}
	}
};