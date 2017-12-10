package it.unibz.inf.mixer_main.execution;

/*
 * #%L
 * mixer-main
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.configuration.ConfParser;
import it.unibz.inf.mixer_main.exception.UnsupportedSystemException;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_shell.core.MixerShell;
import it.unibz.inf.mixer_web.core.MixerWeb;
import it.unibz.inf.utils_options.core.Option;

public class MixerMain extends MixerOptionsInterface{

    private static Logger log = LoggerFactory.getLogger(MixerMain.class);

    private Chrono chrono;
    private Statistics mainStat;
    private Mixer mixer;

    private List<Statistics> threadStatistics;

    public MixerMain(String[] args){
	// Parse command-line options	
	Conf configuration = configure(args);
	instantiateMixer(configuration);
    }

    private Conf configure(String[] args) {
	Option.parseOptions(args);
	String confFile = optConfFile.getValue();
	this.threadStatistics = new ArrayList<Statistics>();
	ConfParser cP = ConfParser.initInstance(confFile);
	
	Conf configuration = new Conf(
		optNumRuns.parsed() ? optNumRuns.getValue() : cP.numRuns().equals("error") ? optNumRuns.getValue() : Integer.valueOf(cP.numRuns()),
		optNumWarmUps.parsed() ? optNumWarmUps.getValue() : cP.numWarmUps().equals("error") ? optNumWarmUps.getValue() : Integer.valueOf(cP.numWarmUps()),
		optTimeout.parsed() ? optTimeout.getValue() : cP.timeout().equals("error") ? optTimeout.getValue() : Integer.valueOf(cP.timeout()),
		optNumClients.parsed() ? optNumClients.getValue() : cP.numClients().equals("error") ? optNumClients.getValue() : Integer.valueOf(cP.numClients()),
		optRewriting.parsed() ? optRewriting.getValue() : cP.rewriting().equals("error") ? optRewriting.getValue() : Boolean.parseBoolean(cP.rewriting()),
		optMode.parsed() ? optMode.getValue() : cP.mode().equals("error") ? optMode.getValue() : cP.mode(),
		optServiceUrl.parsed() ? optServiceUrl.getValue() : cP.serviceUrl(),
		optOwlFile.parsed() ? optOwlFile.getValue() : cP.owlFile(), 
		optMappingsFile.parsed() ? optMappingsFile.getValue() : cP.mappingsFile(), 
		optDbDriverClass.parsed() ? optDbDriverClass.getValue() : cP.dbDriverClass(),
		optDbUrl.parsed() ? optDbUrl.getValue() : cP.dbURL(), 
		optDbUsername.parsed() ? optDbUsername.getValue() : cP.dbUsername(),
		optDbPassword.parsed() ? optDbPassword.getValue() : cP.dbPassword(), 
		optLogFile.parsed() ? optLogFile.getValue() : cP.logPath().equals("error") ? optLogFile.getValue() : cP.logPath(), 
		optQueriesDir.parsed() ? optQueriesDir.getValue() : cP.queriesDir().equals("error") ? optQueriesDir.getValue() : cP.queriesDir(),
		optShellCmd.parsed() ? optShellCmd.getValue() : cP.shellCmd(),
		optShellOut.parsed() ? optShellOut.getValue() : Boolean.parseBoolean(cP.shellOut()),
		optForceTimeouts.parsed() ? optForceTimeouts.getValue() : cP.forceTimeouts(),
		optForcedTimeoutsValue.parsed() ? optForcedTimeoutsValue.getValue() : cP.forcedTimeoutsValue().equals("error") ? optForcedTimeoutsValue.getValue() : Integer.valueOf(cP.forcedTimeoutsValue()),
		optJavaApiClass.parsed() ? optJavaApiClass.getValue() : cP.javaAPIClass().equals("error") ? optJavaApiClass.getValue() : cP.javaAPIClass()
		);
	return configuration;
    }

    /** Modify this method to add other systems **/
    private void instantiateMixer(Conf configuration) {

	String mode = optMode.getValue();

	switch(mode){
	case "java-api" : 
	    try {
		this.mixer = instantiateOwlapiMixer(configuration);
		if( configuration.rewriting() ) this.mixer.rewritingON();
	    } catch (Exception e) {
		String msg = "Error: The class " + configuration.getJavaAPIClass() + " provided as java-api handler does not exist";
		MixerMain.closeEverything(msg, e);
	    }
	    break;
	case "web" : 
	    this.mixer = instantiateWebMixer(configuration);
	    break;
	case "shell" :
	    this.mixer = instantiateShellMixer(configuration);
	    break;
	}
	if( this.mixer == null ){
	    try {
		throw new UnsupportedSystemException("The string "+mode+" is not a valid parameter");
	    } catch (UnsupportedSystemException e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}

    }

    private Mixer instantiateShellMixer(Conf configuration) {
	Mixer result = new MixerShell(configuration);
	return result;
    }

    private Mixer instantiateWebMixer(Conf configuration) {
	Mixer result = new MixerWeb(configuration);
	return result;
    }

    private Mixer instantiateOwlapiMixer(Conf configuration) 
	    throws UnsupportedSystemException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
	Class<?> clazz= Class.forName( configuration.getJavaAPIClass() );
	Constructor<?> ctor = clazz.getConstructor(Conf.class);
	Mixer result = (Mixer)ctor.newInstance(new Object[] { configuration });

	return result;
    }

    private void do_tests(){

	mainStat = new Statistics("GLOBAL");
	chrono = new Chrono();

	// Load the system
	chrono.start();
	mixer.load();
	mainStat.getSimpleStatsInstance("main").addTime("load-time", chrono.stop());

	List<MixerThread> threads = setUpMixerThreads();
	test(threads);

	logStatistics();
    }


    private void logStatistics() {

	//		// Join statistics
	//		for( Statistics s : threadStatistics ){
	//			mainStat.merge(s);
	//		}

	FileWriter statsWriter = getLogWriter();
	try {
	    statsWriter.write(mainStat.printStats());
	    statsWriter.flush();

	    for( Statistics s : threadStatistics ){
		statsWriter.write(s.printStats());
	    }

	    statsWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void test(List<MixerThread> threads) {
	for( MixerThread mT : threads){
	    mT.start();
	}
	for( MixerThread mT : threads ){
	    try {
		mT.join();				
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    private FileWriter getLogWriter() {
	String statsFileName = mixer.getConfiguration().getLogFile();
	File statsFile = new File(statsFileName);
	if( statsFile.exists() ) statsFile.delete();
	FileWriter statsWriter = null;
	try {
	    statsWriter = new FileWriter(statsFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return statsWriter;
    }

    private List<MixerThread> setUpMixerThreads(){

	List<MixerThread> threads = new ArrayList<MixerThread>();
	//		boolean rwAndUnf = false;
	//		if( numClients == 1 ){
	//			rwAndUnf = true;
	//		}

	File folder = new File(mixer.getConfiguration().getTemplatesDir());
	File[] listOfFiles = folder.listFiles();

	for( int i = 0; i < mixer.getConfiguration().getNumClients(); ++i ){
	    // Configure each mixerThread
	    Statistics stat = new Statistics("thread#"+i);
	    this.threadStatistics.add(stat);
	    MixerThread mT = new MixerThread(mixer, stat, listOfFiles);
	    threads.add(mT);
	}

	return threads;
    }

    public static void main( String[] args ){

	MixerMain main = new MixerMain(args);
	main.do_tests();

    }

    public static void closeEverything(String msg, Exception e) {
	log.error(msg);
	throw new RuntimeException(e);
    }
};

