package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionDB2 extends DBMSConnection{

    public DBMSConnectionDB2(String dbUrl, String dbUser, String dbPwd, String dbDriverClass) throws ClassNotFoundException, SQLException {
        super(dbUrl, dbUser, dbPwd);
        jdbcConnector = "jdbc:db2";
        Class.forName(dbDriverClass);
        connect();
    }

    @Override
    protected void connect() throws SQLException {
        connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);
    }
}
