package it.unibz.inf.mixer_interface.core;

import it.unibz.inf.mixer_interface.configuration.Conf;

/**
 * 
 * @author Davide Lanti
 *
 */
public abstract class Mixer {
	
	// Configuration parameters
	protected Conf configuration;
		
	public Mixer(Conf configuration){
		this.configuration = configuration;
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
	 * @param timeout
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
	 * @return The query rewritten in order to 
	 *         take into account for reasoning.
	 */
	public abstract String getRewriting();
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
	
	// **************** END OF Abstract Methods Section ****************** //
	
	public Conf getConfiguration(){
		return configuration;
	}
	
};
