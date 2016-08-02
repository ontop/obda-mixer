package it.unibz.inf.mixer_main.statistics;

/*
 * #%L
 * mixer-main
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