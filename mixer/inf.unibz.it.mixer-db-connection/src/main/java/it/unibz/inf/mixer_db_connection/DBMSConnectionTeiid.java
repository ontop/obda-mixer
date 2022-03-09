package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionTeiid extends DBMSConnection{

        public DBMSConnectionTeiid(Conf conf) throws ClassNotFoundException, SQLException {
            super(conf);
            String driverClass = conf.getDriverClass();
            Class.forName(driverClass);
            connect();
        }

        protected void connect() throws SQLException {
            connection = DriverManager.getConnection(databaseUrl, username, password);
        }
}
