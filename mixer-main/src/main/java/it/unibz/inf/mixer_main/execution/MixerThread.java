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
import it.unibz.inf.mixer_main.utils.*;

public class MixerThread extends Thread {
	
	// Logging
	private Statistics stat;
	private Mixer mixer;
	
	private int nRuns; // Number of total runs
	private int nWUps; // Number of warm-up runs
	private int timeout; // timeout time
	
	public MixerThread(Mixer m, int nRuns, int nWUps, int timeout, Statistics stat, File[] listOfFiles){
		this.stat = stat;
		this.mixer = m;
		this.nRuns = nRuns;
		this.nWUps = nWUps;
		this.timeout = timeout;
	}
	
	public void setUp(){
		// What shall I do here?
	}
	
	 public void run() {
		 
		 
		 
		 // Establish the connection
		 DBMSConnection db = new DBMSConnection(mixer.getConfiguration());
		 
		 // Warm up
		 warmUp(listOfFiles);
		 
				 
				 // Access the templates
				 // replace
		 // do the warm up (therefore, DO NOT collect stats in this phase)
		 // do the tests
		 
		 // It should launch an "execution thread, TO BE KILLED if it > timeout"
		 String query = null;
		 mixer.query(query);
     }
	
	 private void warmUp(File[] listOfFiles) {
		 for( int j = 0; j < nWUps; ++j ){
			 for( int i = 0; i < listOfFiles.length; ++i ){				 
				 if( !listOfFiles[i].isFile() ) continue;
				 
				 String sparqlTemplate = 
				 
				 String inFile = mixer.getConfiguration().getTemplatesDir() + listOfFiles[i].getName();
				 String confFile = mixer.getConfiguration().getTemplatesConfDir() + listOfFiles[i].getName();
				 BufferedReader in = new BufferedReader(new FileReader(inFile));
				 
				 StringBuilder queryBuilder = new StringBuilder();
				 String curLine = null;
				 
				 while( (curLine = in.readLine()) != null ){
					 queryBuilder.append(curLine + "\n");
				 }
				 in.close();
				 Template sparqlQueryTemplate = new Template(queryBuilder.toString(), "$");
				 
				 // Get the placeholders
				 in = new BufferedReader(new FileReader(confFile));
				 
				 List<QualifiedName> qNames = new ArrayList<QualifiedName>();
				 while( (curLine = in.readLine()) != null ){
					 qNames.add(new QualifiedName(curLine));
				 }
					
				 in.close();
				 
				 // Find a mix
				 fillPlaceholders(sparqlQueryTemplate, qNames);
				 
				 mixer.query(sparqlQueryTemplate.getFilled());
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
	
	
	public TemplateQuerySelector(Conf configuration){
		index = 0;
		templatesDir = configuration.getTemplatesDir();
		templatesConfDir = configuration.getTemplatesConfDir();
		
		
		// Query templates
		File folder = new File(templatesDir);
		listOfFiles = folder.listFiles();
	}
	
	public String getNextQuery(){
		
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

			 PreparedStatement stmt = db.getPreparedStatement(query);

			 try {
				 ResultSet rs = stmt.executeQuery();

				 if ( !rs.next() ){
					 stmt.close();
					 query = "SELECT DISTINCT " + qN.getSecond() + " FROM " 
							 + qN.getFirst() + " LIMIT " + 0 + ", 1";
					 resultSetPointer.put(qN.toString(), 1);

					 stmt = db.getPreparedStatement(query);

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
}
