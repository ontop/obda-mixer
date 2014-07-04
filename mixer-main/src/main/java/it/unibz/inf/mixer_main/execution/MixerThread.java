package it.unibz.inf.mixer_main.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
	
	private Chrono chrono;
	
	public MixerThread(Mixer m, int nRuns, int nWUps, int timeout, Statistics stat, File[] listOfFiles){
		this.stat = stat;
		this.mixer = m;
		this.nRuns = nRuns;
		this.nWUps = nWUps;
		this.timeout = timeout;
		
		chrono = new Chrono();
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
	
	 private void test(TemplateQuerySelector tqs) {
		// TODO Auto-generated method stub
		
	}

	private void warmUp(TemplateQuerySelector tqs) {
		 for( int j = 0; j < nWUps; ++j ){
			 boolean stop = false;
			 while( !stop ){
				 String query = tqs.getNextQuery();
				 if( query == null ){//
					 stop = true;
				 }
				 else{
					 if( timeout == 0 ) mixer.query(query); else mixer.query(query, timeout);
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
	
	public String getNextQuery(){
		
		if( index >= listOfFiles.length -1 ) return null;
		
		while( !listOfFiles[index++].isFile() ); 
		
		String inFile = templatesDir + listOfFiles[index].getName();
		String confFile = templatesConfDir + listOfFiles[index].getName();
		
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