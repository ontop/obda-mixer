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
        String driver = this.getConfiguration().getDriverClass();
        Conf conf = this.getConfiguration();
            switch(driver) {
                case DBType.MYSQL:
                    conn = new DBMSConnectionMysql(conf);
                    break;
                case DBType.POSTGRES:
                    conn = new DBMSConnectionPostgres(conf);
                    break;
                case DBType.SQLSERVER:
                    conn = new DBMSConnectionMSSQL(conf);
                    break;
                case DBType.DB2:
                    conn = new DBMSConnectionDB2(conf);
                    break;
                case DBType.TEIID:
                    conn = new DBMSConnectionTeiid(conf);
                    break;
            }
    }

    @Override
    public void load() throws Exception {
        // Unsupported
    }

    @Override
    public Object executeQuery(String query) {
        ResultSet res = null;
        try {
            PreparedStatement stmt = conn.getConnection().prepareStatement(query);
            res =  stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Object executeQuery(String query, int timeout) {
        return null;
    }

    @Override
    public int traverseResultSet(Object resultSet) {
        int cnt = 0;
        if( resultSet instanceof  ResultSet ){
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
    public void executeWarmUpQuery(String query) {
        try {
            PreparedStatement stmt = conn.getConnection().prepareStatement(query);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeWarmUpQuery(String query, int timeout) {

    }
}
