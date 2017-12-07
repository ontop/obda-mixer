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

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.inf.mixer_main.execution.MixerMain;

/**
 * Reads the configuration info from a configuration file.
 * @author Davide Lanti
 *
 */
public class ConfParser {
	
    
    private static ConfParser instance = null;
    private static Logger log = LoggerFactory.getLogger(ConfParser.class);
    
	protected String confFile;
	
	protected ConfParser(String resourcesDir){
		this.confFile = resourcesDir + "/configuration.conf";
	};
	
	public static ConfParser getInstance(){
	    
	    assert instance != null : "Call method initInstance() first.";
	    
	    return instance;
	}
	
	public static ConfParser initInstance(String resourcesDir){
	    instance = new ConfParser(resourcesDir);
	    return instance;
	}
	
	public String dbDriver() {
	    return searchTag("driver-class");
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
	
	public String getJavaAPIClass(){
	    return searchTag("java-api-class");
	}
	
	/** @return Path for OBDA System Executable (only in Shell mode) */
	public String getShellCmd() {
	    return searchTag("shell-cmd");
	}
	
	/** @return Specify what to do with the output of the shell command */
	public String getShellOutput() {
	    return searchTag("shell-out");
	}
	
	/** @return Templates for which we force a timeout */
	public String getForcedTimeouts() {
	    return searchTag("forced-timeouts");
	}
	
	/** @return Templates for which we force a timeout */
	public String getForcedTimeoutsTimeoutValue() {
	    return searchTag("forced-timeouts-timeout-value");
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
		String msg = "I could not find the "
			+ "configuration file, currently set at \"" + confFile + "\". To specify "
			+ "another path, use the --conf option.";
		MixerMain.closeEverything(msg,e);
	    }
	    return "error";
	}
	
}
