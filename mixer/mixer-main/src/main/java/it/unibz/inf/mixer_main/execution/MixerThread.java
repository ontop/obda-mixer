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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.statistics.SimpleStatistics;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_main.utils.*;

public class MixerThread extends Thread {
	
	// Logging
	private Statistics stat;
	private Mixer mixer;
	
	private int nRuns; // Number of total runs
	private int nWUps; // Number of warm-up runs
	private int timeout; // timeout time
	
	// Do I (me, thread) have to collect rewriting and unfolding time?
	boolean rwAndUnf = false;
	
	// Time statistics
	private Chrono chrono;
	private Chrono chronoMix;
	
	public MixerThread(Mixer m, int nRuns, int nWUps, int timeout, Statistics stat, File[] listOfFiles, boolean rwAndUnf){
		this.stat = stat;
		this.mixer = m;
		this.nRuns = nRuns;
		this.nWUps = nWUps;
		this.timeout = timeout;
		
		chrono = new Chrono();
		chronoMix = new Chrono();
		
		this.rwAndUnf = rwAndUnf;
	}
	
	public void setUp(){
		// What shall I do here?
	}
	
	 public void run() {
		 
		 // Establish the connection
		 DBMSConnection db = new DBMSConnection(mixer.getConfiguration());
		 
		 TemplateQuerySelector tqs = new TemplateQuerySelector(mixer.getConfiguration(), db);
		 
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
			 long timeWasted = 0;
			 chronoMix.start();
			 SimpleStatistics localStat = stat.getSimpleStatsInstance("run#"+j);
			 boolean stop = false;
			 while( !stop ){
				 chrono.start();
				 String query = tqs.getNextQuery();
				 timeWasted += chrono.stop();
				 if( query == null ){
					 stop = true;
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
					 }
					 timeWasted += chrono.stop();
				 }
			 }
			 // mix time
			 localStat.addTime("mix_time#"+j, chronoMix.stop() - timeWasted);
		 }
	 }
	 
	private void warmUp(TemplateQuerySelector tqs) {
		 for( int j = 0; j < nWUps; ++j ){
			 boolean stop = false;
			 while( !stop ){
				 String query = tqs.getNextQuery();
				 if( query == null ){
					 stop = true;
				 }
				 else{
					 if( timeout == 0 ) mixer.executeQuery(query); else mixer.executeQuery(query, timeout);
				 }
			 }
		 }
	 }
}

class DBMSConnection{
	
	// Connection Parameters
	private String jdbcConnector;
	private String databaseUrl;
	private String username;
	private String password;
	
	// A JDBC connection (Note that each MixerThread has its own JDBC connection)
	private Connection connection;
	
	DBMSConnection(Conf conf){
		jdbcConnector = "jdbc:mysql";
		databaseUrl = conf.getDatabaseUrl();
		username = conf.getDatabaseUser();
		password = conf.getDatabasePwd();
		
		String url = 
				jdbcConnector + "://" + databaseUrl 
				+ "?useServerPrepStmts=false&rewriteBatchedStatements=true&user=" + username 
				+ "&password=" + password;
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	Connection getConnection(){
		return connection;
	}
	
}

class TemplateQuerySelector{
	
	private String templatesDir;
	private String templatesConfDir;
	
	// Fields to use this class as an iterator
	private int index;
	private File[] listOfFiles;
	
	// Pointers in the database
	private Map<String, Integer> resultSetPointer = new HashMap<String, Integer>();
	
	// Connection to the database
	DBMSConnection db;
	
	public TemplateQuerySelector(Conf configuration, DBMSConnection db){
		index = 0;
		templatesDir = configuration.getTemplatesDir();
		templatesConfDir = configuration.getTemplatesConfDir();
		
		this.db = db;
		
		// Query templates
		File folder = new File(templatesDir);
		listOfFiles = folder.listFiles();
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
		
		String inFile = templatesDir + "/" + listOfFiles[index].getName(); // TODO Replace with relativePath
		String confFile = templatesConfDir + "/" + listOfFiles[index].getName();
		
		String result = null;
		
		try {
			BufferedReader in;
			in = new BufferedReader(new FileReader(inFile));
			
			StringBuilder queryBuilder = new StringBuilder();
			String curLine = null;
			
			while( (curLine = in.readLine()) != null ){
				queryBuilder.append(curLine + "\n");
			}
			in.close();
			
			Template sparqlQueryTemplate = new Template(queryBuilder.toString(), "$");
			
			in = new BufferedReader(new FileReader(confFile));
			
			List<QualifiedName> qNames = new ArrayList<QualifiedName>();
			while( (curLine = in.readLine()) != null ){
				qNames.add(new QualifiedName(curLine));
			}
			
			in.close();
			
			fillPlaceholders(sparqlQueryTemplate, qNames);
			result = sparqlQueryTemplate.getFilled();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		++index;
		return result;
	}
	
	private void fillPlaceholders(Template sparqlQueryTemplate,
			 List<QualifiedName> qNames) {

		 if(sparqlQueryTemplate.getNumPlaceholders() == 0) return;

		 List<String> fillers = new ArrayList<String>();

		 for(QualifiedName qN : qNames ){

			 int pointer = 0;
			 if( resultSetPointer.containsKey(qN.toString()) ){
				 pointer = resultSetPointer.get(qN.toString());
				 resultSetPointer.put(qN.toString(), pointer + 1);
			 }
			 else{
				 resultSetPointer.put(qN.toString(), 1);
			 }

			 String query = "SELECT DISTINCT " + qN.getSecond() + " FROM " 
					 + qN.getFirst() + " LIMIT " + pointer+ ", 1";
			 
			 Connection conn = db.getConnection();
			 
			 PreparedStatement stmt = null;
			 
			 try {
				 stmt = conn.prepareStatement(query);
			 } catch (SQLException e1) {
				 e1.printStackTrace();
			 }

			 try {
				 ResultSet rs = stmt.executeQuery();

				 if ( !rs.next() ){
					 stmt.close();
					 query = "SELECT DISTINCT " + qN.getSecond() + " FROM " 
							 + qN.getFirst() + " LIMIT " + 0 + ", 1";
					 resultSetPointer.put(qN.toString(), 1);

					 stmt = conn.prepareStatement(query);

					 rs = stmt.executeQuery();
					 if( !rs.next() ){
						 System.err.println("Problem");
					 }
				 }
				 fillers.add( rs.getString(qN.getSecond()) );

			 } catch (SQLException e) {
				 e.printStackTrace();
			 }
		 }
		 for( int i = 1; i <= fillers.size(); ++i ){
			 sparqlQueryTemplate.setNthPlaceholder(i, fillers.get(i-1));
		 }
	 }
};