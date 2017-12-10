package it.unibz.inf.mixer_main.connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionPostgres extends DBMSConnection{
	
	public DBMSConnectionPostgres(Conf conf){
		super(conf);
		jdbcConnector = "jdbc:postgresql";
		connect();
	}
	
	protected void connect(){		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
			
		try {
			connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
		
}