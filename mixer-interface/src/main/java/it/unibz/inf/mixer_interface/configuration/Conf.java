package it.unibz.inf.mixer_interface.configuration;

public class Conf {
	// OBDA connection parameters
	protected final String owlFile;
	protected final String mappingsFile;
	
	// Database credentials
	protected final String databaseUrl;
	protected final String databaseUser;
	protected final String databasePwd;

	// Mixer-specific parameters
	protected final String logFile;
	protected final String templatesDir;
	protected final String templatesConfDir;
	
	public Conf(
			String owlFile, 
			String mappingsFile, 
			String databaseUrl,
			String databaseUser,
			String databasePwd,
			String logFile,
			String templatesDir,
			String templatesConfDir
			){
		this.owlFile = owlFile;
		this.mappingsFile = mappingsFile;
		this.databaseUrl = databaseUrl;
		this.databaseUser = databaseUser;
		this.databasePwd = databasePwd;
		this.logFile = logFile;
		this.templatesDir = templatesDir;
		this.templatesConfDir = templatesConfDir;
	}

	public String getOwlFile() {
		return owlFile;
	}

	public String getMappingsFile() {
		return mappingsFile;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public String getDatabasePwd() {
		return databasePwd;
	}

	public String getLogFile() {
		return logFile;
	}

	public String getTemplatesDir() {
		return templatesDir;
	}

	public String getTemplatesConfDir() {
		return templatesConfDir;
	}
}
