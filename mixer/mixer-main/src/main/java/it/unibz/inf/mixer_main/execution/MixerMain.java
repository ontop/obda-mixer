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
import it.unibz.inf.utils_options.core.BooleanOption;
import it.unibz.inf.utils_options.core.IntOption;
import it.unibz.inf.utils_options.core.Option;
import it.unibz.inf.utils_options.core.StringOption;
import it.unibz.inf.utils_options.core.StringOptionWithRange;
import it.unibz.inf.utils_options.ranges.IntRange;
import it.unibz.inf.utils_options.ranges.StringRange;

public class MixerMain {

    private static Logger log = LoggerFactory.getLogger(MixerMain.class);
    
    private Chrono chrono;
    private Statistics mainStat;
    private ConfParser cP;
    private Mixer mixer;

    // Command-line options
    private IntOption optNumRuns = new IntOption("--runs", "Number of query mix runs.", "Mixer", 1, new IntRange(1, Integer.MAX_VALUE, true, true));
    private IntOption optNumWarmUps = new IntOption("--warm-ups", "Number of warm up runs.", "Mixer", 1, new IntRange(0, Integer.MAX_VALUE, true, true));
    private IntOption optTimeout = new IntOption("--timeout", "Maximum execution time allowed to a query, in seconds. A value of zero means no timeout.", "Mixer", 0, new IntRange(0, Integer.MAX_VALUE, true, true));
    private IntOption optNumClients = new IntOption("--clients", "Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode", "Mixer", 1, new IntRange(1, 64, true, true));
    private BooleanOption optRewriting = new BooleanOption("--rewriting", "On or Off?", "Mixer", false);

    // Command-line option deciding which Mixer implementation should be used
    private StringOptionWithRange optMode = new StringOptionWithRange("--mode", "The operating mode, "
	    + "one of: java api mode (java-api), sparql endpoint mode (web), or shell script mode (shell), ", "Mixer", "java-api", new StringRange("[java-api,web,shell]"));
    private StringOption optServiceUrl = new StringOption("--url", "URL for the SPARQL Endpoint (To be used with --obda=web)", "Mixer", "http://10.7.20.65:2021/sparql/");

    private static StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "src/main/resources");

    // Internal state
    private int numRuns;
    private int numWarmUps;
    private int timeout;
    private int numClients;
    private boolean rewriting;
    private String serviceUrl;
    private List<Statistics> threadStatistics;
    private String resourcesDir;

    public MixerMain(String[] args){
	// Parse command-line options
	Option.parseOptions(args);

	this.numRuns = optNumRuns.getValue();
	this.numWarmUps = optNumWarmUps.getValue();
	this.timeout = optTimeout.getValue();
	this.numClients = optNumClients.getValue();
	this.rewriting = optRewriting.getValue();
	this.serviceUrl = optServiceUrl.getValue();
	this.resourcesDir = optResources.getValue();

	this.threadStatistics = new ArrayList<Statistics>();

	cP = ConfParser.initInstance(resourcesDir);

	Conf configuration = new Conf(
		cP.owlFile(), 
		cP.mappingsFile(), 
		cP.dbDriver(),
		cP.dbUrl(), 
		cP.dbUsername(),
		cP.dbPassword(), 
		cP.getLogPath(), 
		cP.getQueriesDir(),
		cP.getShellCmd(),
		cP.getShellOutput(),
		cP.getForcedTimeouts(),
		cP.getForcedTimeoutsTimeoutValue(),
		cP.getJavaAPIClass()
		);

	instantiateMixer(configuration, rewriting);
    }

    /** Modify this method to add other systems **/
    private void instantiateMixer(Conf configuration, boolean rewriting) {
	
	String mode = optMode.getValue();
	
	switch(mode){
	case "java-api" : 
	    try {
		this.mixer = instantiateOwlapiMixer(configuration);
		if( rewriting ) this.mixer.rewritingON();
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
	Mixer result = new MixerWeb(configuration, serviceUrl);
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
	String statsFileName = cP.getLogPath() + "/statsMixer.txt";
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

	for( int i = 0; i < this.numClients; ++i ){
	    // Configure each mixerThread
	    Statistics stat = new Statistics("thread#"+i);
	    this.threadStatistics.add(stat);
	    MixerThread mT = new MixerThread(mixer, numRuns, numWarmUps, timeout, stat, listOfFiles);
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