package it.unibz.inf.mixer_main.connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.Connection;

public abstract class DBMSConnection{
	
	// Connection Parameters
	protected String jdbcConnector;
	protected String databaseUrl;
	protected String username;
	protected String password;
	
	// A JDBC connection (Note that each MixerThread has its own JDBC connection)
	protected Connection connection;
	
	DBMSConnection(Conf conf){
		databaseUrl = conf.getDatabaseUrl();
		username = conf.getDatabaseUser();
		password = conf.getDatabasePwd();
	}
	
	protected abstract void connect();
	
	public Connection getConnection(){
		return connection;
	}	
	
	public String getJdbcConnector(){
		return this.jdbcConnector;
	}
}