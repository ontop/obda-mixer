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
import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.configuration.ConfParser;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_ontop.core.MixerOntop;
import it.unibz.inf.utils_options.core.BooleanOption;
import it.unibz.inf.utils_options.core.IntOption;
import it.unibz.inf.utils_options.core.Option;
import it.unibz.inf.utils_options.core.StringOptionWithRange;
import it.unibz.inf.utils_options.ranges.IntRange;
import it.unibz.inf.utils_options.ranges.StringRange;

public class MixerMain {
		
	private Chrono chrono;
	private Statistics mainStat;
	private ConfParser cP;
	private Mixer mixer;
	
	// Command-line options
	private IntOption optNumRuns = new IntOption("--runs", "Number of query mix runs.", "Mixer", 1, new IntRange(1, Integer.MAX_VALUE, true, true));
	private IntOption optNumWarmUps = new IntOption("--warm-ups", "Number of warm up runs.", "Mixer", 0, new IntRange(0, Integer.MAX_VALUE, true, true));
	private IntOption optTimeout = new IntOption("--timeout", "Maximum execution time allowed to a query, in seconds. A value of zero means no timeout.", "Mixer", 0, new IntRange(0, Integer.MAX_VALUE, true, true));
	private IntOption optNumClients = new IntOption("--clients", "Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode", "Mixer", 1, new IntRange(1, 64, true, true));
	private BooleanOption optRewriting = new BooleanOption("--rewriting", "On or Off?", "Mixer", false);
	
	// Command-line option deciding which Mixer implementation should be used
	private StringOptionWithRange optOBDASystem = new StringOptionWithRange("--obda", "The OBDA system under test", "Mixer", "ontop", new StringRange("[ontop]"));
	
	// Internal state
	private int numRuns;
	private int numWarmUps;
	private int timeout;
	private int numClients;
	private boolean rewriting;
	private List<Statistics> threadStatistics;
	
	public MixerMain(String[] args){
		cP = ConfParser.getInstance();

		// Parse command-line options
		Option.parseOptions(args);
		
		this.numRuns = optNumRuns.getValue();
		this.numWarmUps = optNumWarmUps.getValue();
		this.timeout = optTimeout.getValue();
		this.numClients = optNumClients.getValue();
		this.rewriting = optRewriting.getValue();
		
		this.threadStatistics = new ArrayList<Statistics>();

		Conf configuration = new Conf(
				cP.owlFile(), 
				cP.mappingsFile(), 
				cP.dbDriver(),
				cP.dbUrl(), 
				cP.dbUsername(),
				cP.dbPassword(), 
				cP.getLogPath(), 
				cP.getQueriesDir() + "/Templates");

		
		instantiateMixer(configuration);
	}
	
	/** Modify this method to add other systems **/
	private void instantiateMixer(Conf configuration) {
		String system = optOBDASystem.getValue();
		
		if( system.equals("ontop") ){
			mixer = new MixerOntop(configuration, rewriting); 
		}
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
		
		// Join statistics
		for( Statistics s : threadStatistics ){
			mainStat.merge(s);
		}
		
		FileWriter statsWriter = getLogWriter();
		try {
			statsWriter.write(mainStat.printStats());
			statsWriter.flush();
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
		boolean rwAndUnf = false;
		if( numClients == 1 ){
			rwAndUnf = true;
		}
		
		File folder = new File(mixer.getConfiguration().getTemplatesDir());
		File[] listOfFiles = folder.listFiles();
		
		for( int i = 0; i < optNumClients.getValue(); ++i ){
			// Configure each mixerThread
			Statistics stat = new Statistics("thread#"+i);
			this.threadStatistics.add(stat);
			MixerThread mT = new MixerThread(mixer, numRuns, numWarmUps, timeout, stat, listOfFiles, rwAndUnf);
			threads.add(mT);
		}
		
		return threads;
	}

	public static void main( String[] args ){

		MixerMain main = new MixerMain(args);
		main.do_tests();

	}
};