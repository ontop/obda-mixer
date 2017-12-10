package it.unibz.inf.mixer_interface.core;

/*
 * #%L
 * mixer-interface
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

import it.unibz.inf.mixer_interface.configuration.Conf;

/**
 * 
 * @author Davide Lanti
 *
 */
public abstract class Mixer {
	
	// Configuration parameters
	protected Conf configuration;
	
	protected boolean rewriting;
		
	public Mixer(Conf configuration){
		this.configuration = configuration;
		this.rewriting = false;
	}
		
	// ******************** Abstract Methods Section ********************* //
	
	// --------------- Operations ---------------- //

	/**
	 * It loads the OBDA system
	 */
	public abstract void load();
	
	/**
	 * Issues a query with an execution timeout
	 * @param query
	 * @return a result set
	 */
	public abstract Object executeQuery(String query);
	
	/**
	 * Issues a query with an execution timeout
	 * @param query
	 * @param timeout (seconds)
	 * @return a result set
	 */
	public abstract Object executeQuery(String query, int timeout);
	
	/**
	 * Traverse the set of results obtained by issuing the query
	 * @return The number of results
	 */
	public abstract int traverseResultSet(Object resultSet);
	// ------------------------------------------- //
	
	// ------------- Time statistics ------------- //
	
	/**
	 * 
	 * @return The time spent by the OBDA system in 
	 *         the rewriting (reasoning) phase, 
	 *         in milliseconds.
	 */
	public abstract long getRewritingTime();
	
	/**
	 * 
	 * @return The time spent by the OBDA system in
	 *         the phase of translation of the 
	 *         ontological query into a query
	 *         over the physical data sources.
	 */
	public abstract long getUnfoldingTime();
	// ------------------------------------------- //
	
	// -------------- Logs ----------------------- //
	
	/**
	 * 
	 * @return The translated <b>SQL</b> query.
	 */
	public abstract String getUnfolding();
	
	/**
	 * 
	 * @return The size of the unfolded query, in terms of 
	 *         number of datalog rules
	 */
	public abstract int getUnfoldingSize();
	
	/**
	 * 
	 * @return The query rewritten in order to 
	 *         take into account for reasoning.
	 */
	public abstract String getRewriting();
	
	/**
	 * 
	 * @return The size of the rewritten query, in terms of 
	 *         number of datalog rules
	 */
	public abstract int getRewritingSize();
	// ------------------------------------------- //
	
		
	// ------------- Configuration --------------- //
	
	/**
	 * It turns off the rewriting.
	 */
	public abstract void rewritingOFF();
	
	/**
	 * It turns on the rewriting.
	 */
	public abstract void rewritingON();
	// ------------------------------------------- //
	
	public abstract void executeWarmUpQuery(String query);
	
	
	public abstract void executeWarmUpQuery(String query, int timeout);	
	
	// **************** END OF Abstract Methods Section ****************** //
	
	public Conf getConfiguration(){
		return configuration;
	}

};
