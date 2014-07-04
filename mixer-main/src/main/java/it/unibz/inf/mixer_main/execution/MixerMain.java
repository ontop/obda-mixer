package it.unibz.inf.mixer_main.execution;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.configuration.ConfParser;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_ontop.core.MixerOntop;
import it.unibz.inf.vig_options.core.IntOption;
import it.unibz.inf.vig_options.core.Option;
import it.unibz.inf.vig_options.ranges.IntRange;

public class MixerMain {
		
	private Chrono chrono;
	private Statistics mainStat;
	private ConfParser cP;
	private Mixer mixer;
	
	// Command-line options
	private IntOption optNumRuns = new IntOption("--runs", "Number of query mix runs.", "Mixer", 50, new IntRange(1, Integer.MAX_VALUE, true, true));
	private IntOption optNumWarmUps = new IntOption("--warm-ups", "Number of warm up runs.", "Mixer", 10, new IntRange(1, Integer.MAX_VALUE, true, true));
	private IntOption optTimeout = new IntOption("--timeout", "Maximum execution time allowed to a query, in seconds.", "Mixer", 60, new IntRange(1, Integer.MAX_VALUE, true, true));
	private IntOption optNumClients = new IntOption("--clients", "Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode", "Mixer", 1, new IntRange(1, 64, true, true));
	
	private void do_tests(String[] args){
		
		cP = ConfParser.getInstance();
		
		// Parse command-line options
		Option.parseOptions(args);
		
		mainStat = new Statistics("GLOBAL");
	
		chrono = new Chrono();
		
		Conf configuration = new Conf(
				cP.owlFile(), 
				cP.mappingsFile(), 
				cP.dbUrl(), 
				cP.dbUsername(),
				cP.dbPassword(), 
				cP.getLogPath(), 
				cP.getQueriesDir() + "\\Templates",
				cP.getQueriesDir() + "\\TemplatesConf");
		
		mixer = new MixerOntop(configuration);
		
		// Load the system
		chrono.start();
		mixer.load();
		mainStat.getSimpleStatsInstance("main").addTime("load-time", chrono.end());
		
		String statsFileName = "src/main/resources/davide/statsMixer.txt";
		File statsFile = new File(statsFileName);
		if( statsFile.exists() ) statsFile.delete();
		FileWriter statsWriter = new FileWriter(statsFile);
		
		setUpMixerThreads(listOfFiles);
	}
	
	
	private List<MixerThread> setUpMixerThreads(listOfFiles){
				
		List<MixerThread> threads = new ArrayList<MixerThread>();
		
		for( int i = 0; i < optNumClients.getValue(); ++i ){
			// Configure each mixerThread
			MixerThread mT = new 
		}
	}
	
	public static void main( String[] args ){
		
		MixerMain main = new MixerMain();
		main.do_tests(args);
		
	}
};