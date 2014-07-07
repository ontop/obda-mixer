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

public class SimpleStatistics {
	private Map<String, Integer> mIntegerStats = new HashMap<String, Integer>();
	private Map<String, Float> mFloatStats = new HashMap<String, Float>();
	private Map<String, Long> mTimeStats = new HashMap<String, Long>();
	private Map<String, Boolean> mBools = new HashMap<String, Boolean>();
	
	private String label;
	
	public void setBoolean(String key, boolean bool){
		mBools.put(key, bool);
	}
	
	public void addTime(String key, long increment){
		if( mTimeStats.containsKey(key) ){
			long temp = mTimeStats.get(key);
			temp += increment;
			mTimeStats.put(key, temp);
		}
		else
			mTimeStats.put(key, increment);
	}
	
	public void setInt(String key, int value){
		mIntegerStats.put(key, value);
	}
	
	public void setFloat(String key, float value){
		mFloatStats.put(key, value);
	}
	
	public void setTime(String key, long time){
		mTimeStats.put(key, time);
	}
	
	public void addInt(String key, int increment){
		if( mIntegerStats.containsKey(key) ){
			int temp = mIntegerStats.get(key);
			temp += increment;
			mIntegerStats.put(key, temp);
		}
		else
			mIntegerStats.put(key, increment);
	}
	
	public void addFloat(String key, float increment){
		if( mFloatStats.containsKey(key) ){
			float temp = mFloatStats.get(key);
			temp += increment;
			mFloatStats.put(key, temp);
		}
		else
			mFloatStats.put(key, increment);
	}
	
	public float getFloatStat(String key){
		return mFloatStats.get(key);
	}
	
	public int getIntStat(String key){
		return mIntegerStats.get(key);
	}
	
	public String printStats(){
		
		StringBuilder result = new StringBuilder();
		
		for( String key : mIntegerStats.keySet() ){
			result.append("["+label+"] "+"[");
			result.append(key);
			result.append("] = ");
			result.append(mIntegerStats.get(key));
			result.append("\n");
		}
		for( String key : mFloatStats.keySet() ){
			result.append("["+label+"] "+"[");
			result.append(key);
			result.append("] = ");
			result.append(mFloatStats.get(key));
			result.append("\n");
		}
		for( String key : mTimeStats.keySet() ){
			result.append("["+label+"] "+"[");
			result.append(key);
			result.append("] = ");
			result.append(mTimeStats.get(key));
			result.append("\n");
		}
		for( String key : mBools.keySet() ){
			result.append("["+label+"] "+"[");
			result.append(key);
			result.append("] = ");
			result.append(mBools.get(key));
			result.append("\n");
		}
		
		return result.toString();
	}
	
	public void setGlobalLabel(String label){
		this.label = label;
	}
	
	public void reset(){
		mFloatStats.clear();
		mIntegerStats.clear();
		mTimeStats.clear();
	}
}
