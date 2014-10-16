package it.unibz.inf.mixer_main.connection;

import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.inf.mixer_interface.configuration.Conf;

public class DBMSConnectionMysql extends DBMSConnection {

	public DBMSConnectionMysql(Conf conf) {
		super(conf);
		jdbcConnector = "jdbc:mysql";
		connect();
	}
	
	protected void connect(){
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

}
