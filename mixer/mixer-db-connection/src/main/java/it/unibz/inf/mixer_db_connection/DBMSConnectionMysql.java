package it.unibz.inf.mixer_db_connection;

import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.inf.mixer_interface.configuration.Conf;

public class DBMSConnectionMysql extends DBMSConnection {

    public DBMSConnectionMysql(String dbUrl, String dbUser, String dbPwd, String dbDriverClass) throws ClassNotFoundException, SQLException {
		super(dbUrl, dbUser, dbPwd);
		jdbcConnector = "jdbc:mysql";
		Class.forName(dbDriverClass);
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
