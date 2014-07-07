package it.unibz.inf.mixer_main.configuration;

/*
 * #%L
 * dataPumper
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

import it.unibz.inf.vig_options.core.StringOption;

import java.io.*;


/**
 * Reads the configuration info from a configuration file.
 * @author Davide Lanti
 *
 */
public class ConfParser {
	
	protected String confFile;
	private static ConfParser instance = null;
	
	private static StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "src/main/resources");
	
	protected ConfParser(String resourcesDir){
		this.confFile = resourcesDir + "/configuration.conf";
	};
	
	public static ConfParser getInstance(){
		if( instance == null ){
			instance = new ConfParser(optResources.getValue());
		}
		return instance;
	}
	
	/** Returns the url of the database **/
	public  String dbUrl(){
		return searchTag("db-url");
	}
	/** Returns the username of the database **/
	public  String dbUsername(){
		return searchTag("db-username");
	}
	/** Returns the password for the database **/
	public  String dbPassword(){
		return searchTag("db-pwd");
	}
	/** Returns the path to the file containing the mappings **/
	public  String mappingsFile(){
		return searchTag("mappings-file");
	}
	/** Returns the path to the owl file **/
	public String owlFile(){
		return searchTag("owl-file");
	}
	/** Returns the path to the directory containing the query templates **/
	public String getQueriesDir(){
		return searchTag("queries-dir");
	}
	/** Returns the path to the log file **/
	public String getLogPath(){
		return searchTag("log-path");
	}
	
	protected  String searchTag(String tag){
		try{
			BufferedReader in = new BufferedReader(
					new FileReader(confFile));
			String s;
			String[] s2 = new String[2];
			while ((s = in.readLine()) != null){
				s2 = s.split("\\s+");
				if (s2[0].equals(tag)){ in.close(); return s2[1]; }
			}
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return "error";
	}
}
