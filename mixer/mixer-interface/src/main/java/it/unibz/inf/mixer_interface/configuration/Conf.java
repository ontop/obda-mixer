package it.unibz.inf.mixer_interface.configuration;

/*
 * #%L
 * mixer-interface
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class Conf {
	// OBDA connection parameters
	protected final String owlFile;
	protected final String mappingsFile;
	
	// Database credentials
	protected final String driverClass;
	protected final String databaseUrl;
	protected final String databaseUser;
	protected final String databasePwd;

	// Mixer-specific parameters
	protected final String logFile;
	protected final String templatesDir;
	
	public Conf(
			String owlFile, 
			String mappingsFile, 
			String driverClass,
			String databaseUrl,
			String databaseUser,
			String databasePwd,
			String logFile,
			String templatesDir
		){
		this.owlFile = owlFile;
		this.mappingsFile = mappingsFile;
		this.driverClass = driverClass;
		this.databaseUrl = databaseUrl;
		this.databaseUser = databaseUser;
		this.databasePwd = databasePwd;
		this.logFile = logFile;
		this.templatesDir = templatesDir;
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

	public String getDriverClass() {
	    return this.driverClass;
	}
}
