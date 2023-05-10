package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionPostgres extends DBMSConnection {

	public DBMSConnectionPostgres(String dbUrl, String dbUser, String dbPwd, String dbDriverClass) throws ClassNotFoundException, SQLException {
		super(dbUrl, dbUser, dbPwd);
		jdbcConnector = "jdbc:postgresql";
		Class.forName(dbDriverClass);
		connect();
	}

	protected void connect() throws SQLException {
		connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);
    }
}