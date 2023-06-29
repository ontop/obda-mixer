package it.unibz.inf.mixer_jdbc.core;

import it.unibz.inf.mixer_db_connection.*;
import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MixerJDBC extends Mixer {

    DBMSConnection conn;

    public MixerJDBC(Conf conf) throws SQLException, ClassNotFoundException {
        super(conf);
        establishConnection();
    }

    private void establishConnection() throws SQLException, ClassNotFoundException {
        Conf conf = this.getConfiguration();
        String url = conf.getJdbcModeDatabaseUrl();
        String user = conf.getJdbcModeDatabaseUser();
        String pwd = conf.getJdbcModeDatabasePwd();
        String driver = conf.getJdbcModeDriverClass();
            switch(driver) {
                case DBType.MYSQL:
                    conn = new DBMSConnectionMysql(url, user, pwd, driver);
                    break;
                case DBType.POSTGRES:
                    conn = new DBMSConnectionPostgres(url, user, pwd, driver);
                    break;
                case DBType.SQLSERVER:
                    conn = new DBMSConnectionMSSQL(url, user, pwd, driver);
                    break;
                case DBType.DB2:
                    conn = new DBMSConnectionDB2(url, user, pwd, driver);
                    break;
                case DBType.TEIID:
                    conn = new DBMSConnectionTeiid(url, user, pwd, driver);
                    break;
            }
    }

    @Override
    public void load() throws Exception {
        // Unsupported
    }

    @Override
    public Object executeQuery(String query, int timeout) {
        // TODO: timeout currently ignored?
        ResultSet res = null;
        try {
            PreparedStatement stmt = conn.getConnection().prepareStatement(query);
            if (timeout > 0) {
                stmt.setQueryTimeout(timeout);
            }
            res =  stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public int traverseResultSet(Object resultSet) {
        int cnt = 0;
        if( resultSet instanceof ResultSet ){
            ResultSet rs = (ResultSet) resultSet;
            try {
                while(rs.next()){
                    ++cnt;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return cnt;
    }

    @Override
    public long getRewritingTime() {
        return 0;
    }

    @Override
    public long getUnfoldingTime() {
        return 0;
    }

    @Override
    public String getUnfolding() {
        return null;
    }

    @Override
    public int getUnfoldingSize() {
        return 0;
    }

    @Override
    public String getRewriting() {
        return null;
    }

    @Override
    public int getRewritingSize() {
        return 0;
    }

    @Override
    public void rewritingOFF() {

    }

    @Override
    public void rewritingON() {

    }

    @Override
    public void executeWarmUpQuery(String query, int timeout) {
        try {
            PreparedStatement stmt = conn.getConnection().prepareStatement(query);
            if (timeout != 0) {
                stmt.setQueryTimeout(timeout);
            }
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
