package it.unibz.inf.mixer_main.execution;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.connection.DBMSConnection;
import it.unibz.inf.mixer_main.connection.DBMSConnectionMysql;
import it.unibz.inf.mixer_main.connection.DBMSConnectionPostgres;
import it.unibz.inf.mixer_main.statistics.SimpleStatistics;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_main.utils.QualifiedName;
import it.unibz.inf.mixer_main.utils.Template;

public class MixerThread extends Thread {

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    // Logging
    private Statistics stat;
    private Mixer mixer;

    private int nRuns; // Number of total runs
    private int nWUps; // Number of warm-up runs
    private int timeout; // timeout time

    // Do I (me, thread) have to collect rewriting and unfolding time?
    boolean rwAndUnf = true; // TODO Remove

    // Time statistics
    private Chrono chrono;
    private Chrono chronoMix;

    public MixerThread(Mixer m, Statistics stat, File[] listOfFiles){
	this.stat = stat;
	this.mixer = m;
	this.nRuns = m.getConfiguration().getNumRuns();
	this.nWUps = m.getConfiguration().getNumWarmUps();
	this.timeout = m.getConfiguration().getTimeout();

	chrono = new Chrono();
	chronoMix = new Chrono();

	//		this.rwAndUnf = rwAndUnf;
    }

    public void setUp(){
	// What shall I do here?
    }

    public void run() {

	// Establish the connection
	Conf conf = mixer.getConfiguration();
	String driver = conf.getDriverClass();

	DBMSConnection db = null;
	if( driver.equals(MYSQL_DRIVER) ){
	    db = new DBMSConnectionMysql(conf);
	}
	else{
	    db = new DBMSConnectionPostgres(conf);
	}
	TemplateQuerySelector tqs = new TemplateQuerySelector(conf, db);

	// Warm up
	warmUp(tqs); 

	// The actual tests
	test(tqs);
    }

    /**
     * It performs the mixes, and collects the statistics
     * @param tqs
     */
    private void test(TemplateQuerySelector tqs) {

	for( int j = 0; j < nRuns; ++j ){
	    long forcedTimeoutsSum = 0;
	    long timeWasted = 0;
	    chronoMix.start();
	    //			 int i = 0;
	    SimpleStatistics localStat = stat.getSimpleStatsInstance("run#"+j);
	    boolean stop = false;
	    while( !stop ){
		chrono.start();
		String query = tqs.getNextQuery();
		System.out.println(query);
		
		timeWasted += chrono.stop();
		if( query == null ){
		    stop = true;
		}
		else if( query.equals("force-timeout") ){
		    int forcedTimeout = mixer.getConfiguration().getForcedTimeoutsTimeoutValue();
		    localStat.addTime("execution_time#"+tqs.getCurQueryName(), forcedTimeout*1000); // Convert to milliseconds
		    forcedTimeoutsSum += forcedTimeout*1000; // Convert to milliseconds
		}
		else{
		    Object resultSet = null;
		    chrono.start();

		    if( timeout == 0 ) resultSet = mixer.executeQuery(query); else resultSet = mixer.executeQuery(query, timeout);
		    localStat.addTime("execution_time#"+tqs.getCurQueryName(), chrono.stop());
		    chrono.start();
		    int numResults = mixer.traverseResultSet(resultSet);
		    localStat.addTime("resultset_traversal_time#"+tqs.getCurQueryName(), chrono.stop());
		    localStat.addInt("num_results#"+tqs.getCurQueryName(), numResults);
		    chrono.start();
		    if( this.rwAndUnf ){
			localStat.addTime("rewriting_time#"+tqs.getCurQueryName(), mixer.getRewritingTime());
			localStat.addTime("unfolding_time#"+tqs.getCurQueryName(), mixer.getUnfoldingTime());

			// Get query statistics
			if( this.nRuns == 1 && this.nWUps == 0 ){
			    localStat.setInt("rewritingUCQ_size#"+tqs.getCurQueryName(),mixer.getRewritingSize());
			    localStat.setInt("unfoldingUCQ_size#"+tqs.getCurQueryName(),mixer.getUnfoldingSize());
			}
		    }
		    timeWasted += chrono.stop();
		}
	    }
	    // mix time
	    localStat.addTime("mix_time#"+j, chronoMix.stop() - timeWasted + forcedTimeoutsSum);
	}
    }

    private void warmUp(TemplateQuerySelector tqs) {
	for( int j = 0; j < nWUps; ++j ){
	    boolean stop = false;
	    while( !stop ){
		String query = tqs.getNextQuery();
		System.out.println(query);
		if( query == null ){
		    stop = true;
		}
		else{
		    if( !query.equals("force-timeout") ){
			if( timeout == 0 ) mixer.executeWarmUpQuery(query); else mixer.executeWarmUpQuery(query, timeout);
		    }
		}
	    }
	}
    }
};

class TemplateQuerySelector{

    private Set<String> executedQueries = new HashSet<>();

    private String templatesDir;

    // Fields to use this class as an iterator
    private int index;
    private File[] listOfFiles;

    // Pointers in the database
    private Map<String, Integer> resultSetPointer = new HashMap<String, Integer>();

    // Connection to the database
    DBMSConnection db;

    // State
    private int nExecutedTemplate;

    // Queries to skip
    private List<String> forceTimeoutQueries;
    
    public TemplateQuerySelector(Conf configuration, DBMSConnection db){
	index = 0;
	templatesDir = configuration.getTemplatesDir();

	this.db = db;

	// Query templates
	File folder = new File(templatesDir);
	listOfFiles = folder.listFiles();

	this.nExecutedTemplate = 0;
	
	// Force timeouts
	this.forceTimeoutQueries = configuration.getForcedTimeouts();
    }

    public String getCurQueryName(){
	return listOfFiles[index-1].getName();
    }

    public String getNextQuery(){

	if(index >= listOfFiles.length){
	    index = 0;
	    return null;
	}

	while( !listOfFiles[index].isFile() ){
	    ++index;
	    if( index >= listOfFiles.length ){
		index = 0;
		return null;
	    }
	}; 

	String inFile = templatesDir + "/" + listOfFiles[index].getName(); 

	String result = null;

	String queryName = listOfFiles[index].getName();
	if( this.forceTimeoutQueries.contains(queryName) ){
	    // Forcibly timeout the query
	    ++index;
	    return "force-timeout";
	}
	
	try {
	    BufferedReader in;
	    in = new BufferedReader(new FileReader(inFile));

	    StringBuilder queryBuilder = new StringBuilder();
	    String curLine = null;

	    while( (curLine = in.readLine()) != null ){
		queryBuilder.append(curLine + "\n");
	    }
	    in.close();

	    Template sparqlQueryTemplate = new Template(queryBuilder.toString());
	    int maxTries = 200;
	    int i = 0;
	    do{
		fillPlaceholders(sparqlQueryTemplate);
	    }
	    while( i++ < maxTries && sparqlQueryTemplate.getNumPlaceholders() != 0 && executedQueries.contains(sparqlQueryTemplate.getFilled()) );
	    
	    result = sparqlQueryTemplate.getFilled();
	    executedQueries.add(result);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	++index;
	return result;
    }

    private void fillPlaceholders(Template sparqlQueryTemplate) {
	
	System.out.println("[mixer-debug] Call fillPlaceholders");

	Map<Template.PlaceholderInfo, String> mapTIToValue = new HashMap<Template.PlaceholderInfo, String>();

	this.resultSetPointer.clear();
	++this.nExecutedTemplate;

	if(sparqlQueryTemplate.getNumPlaceholders() == 0) return;

	for( int i = 1; i <= sparqlQueryTemplate.getNumPlaceholders(); ++i ){

	    Template.PlaceholderInfo info = sparqlQueryTemplate.getNthPlaceholderInfo(i);

	    String toInsert = null;
	    if( mapTIToValue.containsKey(info) ){
		toInsert = mapTIToValue.get(info);
	    }
	    else{
		toInsert = findValueToInsert( info.getQN() );
		mapTIToValue.put(info, toInsert);
	    }
	    toInsert = info.applyQuote(toInsert, info.quote());
	    sparqlQueryTemplate.setNthPlaceholder(i, toInsert);
	}
    }

    /**
     * Tries to look in the same row, as long as possible
     * @param qN
     * @return
     */
    private String findValueToInsert( QualifiedName qN ) {

	String result = null;
	int pointer = 0;

	if( resultSetPointer.containsKey(qN.toString()) ){
	    pointer = resultSetPointer.get(qN.toString());
	    resultSetPointer.put(qN.toString(), pointer + 1);
	}
	else{
	    resultSetPointer.put(qN.toString(), 1);
	}

	pointer += this.nExecutedTemplate;

	String query = "SELECT " + "*" + " FROM " 
		+ qN.getFirst() + " WHERE " + qN.getSecond() + " IS NOT NULL " 
		+ " LIMIT " + pointer+ ", 1";

	if( db.getJdbcConnector().equals("jdbc:postgresql") ){
	    query = "SELECT \""+
		    "*" +
		    // qN.getSecond()+ 
		    "\" FROM \""+qN.getFirst()+"\" WHERE \""
		    +qN.getSecond()+"\" IS NOT NULL LIMIT 1" +
		    " OFFSET " + pointer;
	}

	Connection conn = db.getConnection();

	PreparedStatement stmt = null;

	try {
	    stmt = conn.prepareStatement(query);
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	try{
	    ResultSet rs = stmt.executeQuery();
	    if ( !rs.next() ){
		stmt.close();
		query = "SELECT  " + "*" + " FROM " 
			+ qN.getFirst() + " LIMIT " + 0 + ", 1";
		resultSetPointer.put(qN.toString(), 1);

		if( db.getJdbcConnector().equals("jdbc:postgresql") ){
		    query = "SELECT  \""+
			    qN.getSecond()+ 
			    "\" FROM \""+qN.getFirst()+"\" WHERE \""
			    +qN.getSecond()+"\" IS NOT NULL LIMIT 1" +
			    " OFFSET " + 0;
		}

		stmt = conn.prepareStatement(query);

		rs = stmt.executeQuery();
		if( !rs.next() ){
		    System.err.println("[QueryMixer.MainThread] Problem: No result to fill placeholder.");
		    System.exit(1);
		}
	    }
	    result = rs.getString(qN.getSecond());
	} catch (SQLException e) {
	    e.printStackTrace();
	    try {
		conn.close();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    System.exit(1);
	}
	return result;
    }
};