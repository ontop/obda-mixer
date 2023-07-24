package it.unibz.inf.mixer_jdbc.core;

import it.unibz.inf.mixer_interface.core.AbstractMixer;
import it.unibz.inf.mixer_interface.core.Handler;
import it.unibz.inf.mixer_interface.core.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class MixerJDBC extends AbstractMixer {

    private Connection conn;

    @Override
    public void init(Map<String, String> conf) throws Exception {
        super.init(conf);

        String url = conf.get("db-url");
        String user = conf.get("db-user");
        String pwd = conf.get("db-pwd");
        String driver = conf.get("db-driverclass");

        try {
            if (driver != null) {
                Class.forName(driver); // Load driver (might be needed for old drivers)
            }
            this.conn = DriverManager.getConnection(url, user, pwd);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to instantiate DB connection", ex);
        }
    }

    @Override
    public void execute(Query query, Handler handler) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            if (query.getTimeout() > 0) {
                stmt.setQueryTimeout(query.getTimeout());
            }
            handler.onSubmit();
            try (ResultSet rs = stmt.executeQuery(query.getString())) {
                handler.onStartResults();
                int cnt = 0;
                while (rs.next()) {
                    ++cnt;
                }
                handler.onEndResults(cnt);
            }
        }
    }

}
