package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionMSSQL extends DBMSConnection {

    public DBMSConnectionMSSQL(Conf conf) throws ClassNotFoundException, SQLException {
        super(conf);
        jdbcConnector = "jdbc:sqlserver";
        String driverClass = conf.getDriverClass();
        Class.forName(driverClass);
        connect();
    }

    @Override
    protected void connect() throws SQLException {
        String dbServer = databaseUrl.substring(0, databaseUrl.lastIndexOf(":"));
        String dbName = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1);
        String dbPort = databaseUrl.substring(databaseUrl.lastIndexOf(":") + 1, databaseUrl.lastIndexOf("/"));
        String connectionUrl = jdbcConnector + "://" + dbServer + ":"
                + dbPort
                + ";databaseName=" + dbName
                + ";user=" + username
                + ";password=" + password
                + ";encrypt=false";
        connection = DriverManager.getConnection(connectionUrl);
    }
}
