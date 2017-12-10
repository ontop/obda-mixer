package it.unibz.inf.mixer_main.connection;

public interface DbType {
	public static final String MYSQL    = "com.mysql.jdbc.Driver";
	public static final String POSTGRES = "org.postgresql.Driver";
	public static final String DB2      = "com.ibm.db2.jcc.DB2Driver";
	public static final String ORACLE   = "oracle.jdbc.OracleDriver";
	public static final String SQLSERVER= "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String H2       = "org.h2.Driver";
}
