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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import it.unibz.inf.mixer_jdbc.core.MixerJDBC;
import it.unibz.inf.mixer_main.statistics.StatisticsCollector;
import it.unibz.inf.mixer_main.statistics.StatisticsManager;
import it.unibz.inf.mixer_main.statistics.StatisticsScope;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.configuration.ConfParser;
import it.unibz.inf.mixer_main.exception.UnsupportedSystemException;
import it.unibz.inf.mixer_main.time.Chrono;
import it.unibz.inf.mixer_shell.core.MixerShell;
import it.unibz.inf.mixer_web.core.MixerWeb;
import it.unibz.inf.utils_options.core.Option;


public class MixerMain extends MixerOptionsInterface {

  private static Logger log = LoggerFactory.getLogger(MixerMain.class);

  private Chrono chrono;
  private StatisticsManager statsMgr;
  private Mixer mixer;

  public MixerMain(String[] args) {
    // Parse command-line options
    Conf configuration = configure(args);
    instantiateMixer(configuration);
  }

  private Conf configure(String[] args) {
    Option.parseOptions(args);
    String confFile = optConfFile.getValue();
    ConfParser cP = ConfParser.initInstance(confFile);

    Conf configuration = new Conf(
            optNumRuns.parsed() ? optNumRuns.getValue() : cP.numRuns().equals("") ? optNumRuns.getValue() : Integer.valueOf(cP.numRuns()),
            optNumWarmUps.parsed() ? optNumWarmUps.getValue() : cP.numWarmUps().equals("") ? optNumWarmUps.getValue() : Integer.valueOf(cP.numWarmUps()),
            optTimeout.parsed() ? optTimeout.getValue() : cP.timeout().equals("") ? optTimeout.getValue() : Integer.valueOf(cP.timeout()),
            optNumClients.parsed() ? optNumClients.getValue() : cP.numClients().equals("") ? optNumClients.getValue() : Integer.valueOf(cP.numClients()),
            optRewriting.parsed() ? optRewriting.getValue() : cP.rewriting().equals("") ? optRewriting.getValue() : Boolean.parseBoolean(cP.rewriting()),
            optLang.parsed() ? optLang.getValue() : cP.lang().equals("") ? optLang.getValue() : cP.lang(),
            optMode.parsed() ? optMode.getValue() : cP.mode().equals("") ? optMode.getValue() : cP.mode(),
            optServiceUrl.parsed() ? optServiceUrl.getValue() : cP.serviceUrl(),
            optOwlFile.parsed() ? optOwlFile.getValue() : cP.owlFile(),
            optMappingsFile.parsed() ? optMappingsFile.getValue() : cP.mappingsFile(),
            optPropertiesFile.parsed() ? optPropertiesFile.getValue() : cP.propertiesFile(),
            optDbDriverClass.parsed() ? optDbDriverClass.getValue() : cP.dbDriverClass(),
            optDbUrl.parsed() ? optDbUrl.getValue() : cP.dbURL(),
            optDbUsername.parsed() ? optDbUsername.getValue() : cP.dbUsername(),
            optDbPassword.parsed() ? optDbPassword.getValue() : cP.dbPassword(),
            optLogFile.parsed() ? optLogFile.getValue() : cP.logFile().equals("") ? optLogFile.getValue() : cP.logFile(),
            optLogImport.parsed() ? optLogImport.getValue() : cP.logImport().equals("") ? optLogImport.getValue() : cP.logImport(),
            optLogImportFilter.parsed() ? optLogImportFilter.getValue() : cP.logImportFilter().equals("") ? optLogImportFilter.getValue() : cP.logImportFilter(),
            optLogImportPrefix.parsed() ? optLogImportPrefix.getValue() : cP.logImportPrefix().equals("") ? optLogImportPrefix.getValue() : cP.logImportPrefix(),
            optQueriesDir.parsed() ? optQueriesDir.getValue() : cP.queriesDir().equals("") ? optQueriesDir.getValue() : cP.queriesDir(),
            optShellCmd.parsed() ? optShellCmd.getValue() : cP.shellCmd(),
            optShellOut.parsed() ? optShellOut.getValue() : Boolean.parseBoolean(cP.shellOut()),
            optForceTimeouts.parsed() ? optForceTimeouts.getValue() : cP.forceTimeouts(),
            optForcedTimeoutsValue.parsed() ? optForcedTimeoutsValue.getValue() : cP.forcedTimeoutsValue().equals("") ? optForcedTimeoutsValue.getValue() : Integer.valueOf(cP.forcedTimeoutsValue()),
            optJavaApiClass.parsed() ? optJavaApiClass.getValue() : cP.javaAPIClass().equals("") ? optJavaApiClass.getValue() : cP.javaAPIClass(),
            optJDBCModeDbDriverClass.parsed() ? optJDBCModeDbDriverClass.getValue() : cP.jdbcModeDBDriverClass(),
            optJDBCModeDbUrl.parsed() ? optJDBCModeDbUrl.getValue() : cP.jdbcModeDBURL(),
            optJDBCModeDbUsername.parsed() ? optJDBCModeDbUsername.getValue() : cP.jdbcModeDBUsername(),
            optJDBCModeDbPassword.parsed() ? optJDBCModeDbPassword.getValue() : cP.jdbcModeDbPassword());
    return configuration;
  }

  /**
   * Modify this method to add other systems
   **/
  private void instantiateMixer(Conf configuration) {

    String mode = configuration.getMode();

    switch (mode) {
      case "java-api":
        try {
          this.mixer = instantiateOwlapiMixer(configuration);
          if (configuration.rewriting()) this.mixer.rewritingON();
        } catch (Exception e) {
          String msg = "Error: The class " + configuration.getJavaAPIClass() + " provided as java-api handler does not exist";
          MixerMain.closeEverything(msg, e);
        }
        break;
      case "web":
        this.mixer = instantiateWebMixer(configuration);
        break;
      case "shell":
        this.mixer = instantiateShellMixer(configuration);
        break;
      case "jdbc":
        this.mixer = instantiateJDBCMixer(configuration);
        break;
    }
    if (this.mixer == null) {
      try {
        throw new UnsupportedSystemException("The string " + mode + " is not a valid parameter");
      } catch (UnsupportedSystemException e) {
        MixerMain.closeEverything("Could not instantiate the OBDA system.", e);
      }
    }
  }

  private Mixer instantiateJDBCMixer(Conf configuration) {
    Mixer result = null;
    try {
      result = new MixerJDBC(configuration);
    } catch (SQLException | ClassNotFoundException e) {
      MixerMain.closeEverything("Failed to instantiate JDBCMixer", e);
    }
    return result;
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
          throws UnsupportedSystemException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Class<?> clazz = Class.forName(configuration.getJavaAPIClass());
    Constructor<?> ctor = clazz.getConstructor(Conf.class);
    Mixer result = (Mixer) ctor.newInstance(new Object[]{configuration});

    return result;
  }

  private void do_tests() {

    statsMgr = new StatisticsManager();
    chrono = new Chrono();

    // Load the system
    chrono.start();
    try {
      mixer.load();
    } catch (Exception e) {
      e.printStackTrace();
      MixerMain.closeEverything("Failed to Load Mixer", e);
    }

    StatisticsCollector globalStats = statsMgr.getCollector(StatisticsScope.global());
    globalStats.add("load-time", chrono.stop());
    globalStats.add("marker", StatisticsScope.processMarker());

    List<MixerThread> threads = setUpMixerThreads();
    test(threads);

    importStatistics();

    logStatistics();
  }

  private void importStatistics() {

    Conf conf = mixer.getConfiguration();
    if (Strings.isNullOrEmpty(conf.getLogImport())) {
      return;
    }

    Path file = Paths.get(conf.getLogImport());
    Pattern filter = Strings.isNullOrEmpty(conf.getLogImportFilter()) ? null : Pattern.compile(conf.getLogImportFilter());
    String prefix = conf.getLogImportPrefix();

    try (Reader in = Files.newBufferedReader(file)) {
      statsMgr.importJson(in, filter, prefix);
    } catch (IOException ex) {
      log.error("Cannot import statistics from " + file, ex);
    }
  }

  private void logStatistics() {
    Path statsFile = Paths.get(mixer.getConfiguration().getLogFile());
    try {
      statsMgr.write(statsFile);
    } catch (IOException e) {
      MixerMain.closeEverything("Cannot write log file " + statsFile, e);
    }
  }

  private void test(List<MixerThread> threads) {
    for (MixerThread mT : threads) {
      mT.start();
    }
    for (MixerThread mT : threads) {
      try {
        mT.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private List<MixerThread> setUpMixerThreads() {

    List<MixerThread> threads = new ArrayList<MixerThread>();
    //		boolean rwAndUnf = false;
    //		if( numClients == 1 ){
    //			rwAndUnf = true;
    //		}

    File folder = new File(mixer.getConfiguration().getTemplatesDir());
    File[] listOfFiles = folder.listFiles();

    for (int i = 0; i < mixer.getConfiguration().getNumClients(); ++i) {
      // Configure each mixerThread
      MixerThread mT = new MixerThread(mixer, statsMgr, listOfFiles, i);
      threads.add(mT);
    }

    return threads;
  }

  public static void main(String[] args) {

    MixerMain main = new MixerMain(args);
    main.do_tests();

  }

  public static <T> T closeEverything(String msg, Exception e) {

    log.error(msg);
    throw new RuntimeException(e);
  }

  public static void closeEverything(String msg) {
    log.error(msg);
    throw new RuntimeException();
  }
};

