package it.unibz.inf.mixer_db_connection;

import it.unibz.inf.mixer_interface.configuration.Conf;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnectionTeiid extends DBMSConnection{

        public DBMSConnectionTeiid(String dbUrl, String dbUser, String dbPwd, String dbDriverClass) throws ClassNotFoundException, SQLException {
            super(dbUrl, dbUser, dbPwd);
            Class.forName(dbDriverClass);
            connect();
        }

        protected void connect() throws SQLException {
            connection = DriverManager.getConnection(databaseUrl, username, password);
        }
}
