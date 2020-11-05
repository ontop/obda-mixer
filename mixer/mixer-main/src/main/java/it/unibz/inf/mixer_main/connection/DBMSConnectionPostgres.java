package it.unibz.inf.mixer_main.connection;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_main.execution.MixerMain;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMSConnectionPostgres extends DBMSConnection{

	public DBMSConnectionPostgres(Conf conf){
		super(conf);
		jdbcConnector = "jdbc:postgresql";
		String driverClass = conf.getDriverClass();
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			MixerMain.closeEverything("Could not find the postgres driver class", e);
		}
		connect();
	}

	protected void connect(){
		try {
			connection = DriverManager.getConnection(jdbcConnector + "://" + databaseUrl, username, password);

			// Set provenance;
			PreparedStatement stmt = connection.prepareStatement("set provsql.where_provenance=off");
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }

}