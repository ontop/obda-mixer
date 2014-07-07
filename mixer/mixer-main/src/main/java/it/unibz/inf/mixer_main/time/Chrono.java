package it.unibz.inf.mixer_main.time;

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
