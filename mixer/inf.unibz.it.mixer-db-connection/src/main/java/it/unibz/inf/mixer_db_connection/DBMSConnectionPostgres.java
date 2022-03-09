package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionPostgres extends DBMSConnection {

	public DBMSConnectionPostgres(Conf conf) throws ClassNotFoundException, SQLException {
		super(conf);
		jdbcConnector = "jdbc:postgresql";
		String driverClass = conf.getDriverClass();
		Class.forName(driverClass);
		connect();
	}

	protected void connect() throws SQLException {
		connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);
    }
}