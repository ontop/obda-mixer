package it.unibz.inf.mixer_db_connection;

public interface DBType {
	String MYSQL    = "com.mysql.jdbc.Driver";
	String POSTGRES = "org.postgresql.Driver";
	String DB2      = "com.ibm.db2.jcc.DB2Driver";
	String ORACLE   = "oracle.jdbc.OracleDriver";
	String SQLSERVER= "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	String H2       = "org.h2.Driver";
	String TEIID    = "org.teiid.jdbc.TeiidDriver";
}
