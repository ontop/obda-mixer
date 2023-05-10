package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DBMSConnection{
	
	// Connection Parameters
	protected String jdbcConnector;
	protected String databaseUrl;
	protected String username;
	protected String password;
	
	// A JDBC connection (Note that each MixerThread has its own JDBC connection)
	protected Connection connection;
	
	DBMSConnection(String dbUrl, String dbUser, String dbPwd){
		databaseUrl = dbUrl;
		username = dbUser;
		password = dbPwd;
	}
	
	protected abstract void connect() throws SQLException;
	
	public Connection getConnection(){
		return connection;
	}	
	
	public String getJdbcConnector(){
		return this.jdbcConnector;
	}
}