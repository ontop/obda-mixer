package it.unibz.inf.mixer_db_connection;

import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.inf.mixer_interface.configuration.Conf;

public class DBMSConnectionMysql extends DBMSConnection {

    public DBMSConnectionMysql(Conf conf) throws ClassNotFoundException, SQLException {
		super(conf);
		jdbcConnector = "jdbc:mysql";
		String driverClass = conf.getDriverClass();
		Class.forName(driverClass);
		connect();
	}

    protected void connect() throws SQLException {
	String url = 
		jdbcConnector + "://" + databaseUrl 
		+ "?useServerPrepStmts=false&rewriteBatchedStatements=true&user=" + username 
		+ "&password=" + password;

	    connection = DriverManager.getConnection(url, username, password);
    }

}
