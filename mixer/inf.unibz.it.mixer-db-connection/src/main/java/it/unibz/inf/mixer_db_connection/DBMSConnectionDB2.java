package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionDB2 extends DBMSConnection{

    public DBMSConnectionDB2(Conf conf) throws ClassNotFoundException, SQLException {
        super(conf);
        jdbcConnector = "jdbc:db2";
        String driverClass = conf.getDriverClass();
        Class.forName(driverClass);
        connect();
    }

    @Override
    protected void connect() throws SQLException {
        connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);
    }
}
