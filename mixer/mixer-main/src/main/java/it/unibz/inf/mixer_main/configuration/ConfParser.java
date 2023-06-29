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
import java.util.Arrays;
import java.util.List;

import it.unibz.inf.mixer_main.execution.MixerMain;

/**
 * Reads the configuration info from a configuration file.
 *
 * @author Davide Lanti
 */
public class ConfParser {

  private static ConfParser instance = null;
  protected String confFile;

  protected ConfParser(String confFile) {
    this.confFile = confFile;
  }

  ;

  public static ConfParser getInstance() {

    assert instance != null : "Call method initInstance() first.";
    return instance;
  }

  public static ConfParser initInstance(String confFile) {
    instance = new ConfParser(confFile);
    return instance;
  }

  /** Returns the driver class of the database for instantiating the query-templates **/
  public String dbDriverClass() {
    return searchTag("driver-class");
  }

  /** In JDBC-mode, driver class of the DB to be queried   */
  public String jdbcModeDBDriverClass() {
    return searchTag("jdbc-mode-driver-class");
  }

  /** Returns the url of the database for instantiating the query-templates **/
  public String dbURL() {
    return searchTag("db-url");
  }

  /** In JDBC-mode, the url of the database to be queried **/
  public String jdbcModeDBURL() {
    return searchTag("jdbc-mode-db-url");
  }

  /** Returns the username of the database for instantiating the query-templates **/
  public String dbUsername() {
    return searchTag("db-username");
  }

  /** Returns the username of the database to be queried **/
  public String jdbcModeDBUsername() {
    return searchTag("jdbc-mode-db-username");
  }

  /** Returns the password for the database **/
  public String dbPassword() {
    return searchTag("db-pwd");
  }

  /** In JDBC-mode, returns the password of the database to be queried **/
  public String jdbcModeDbPassword() {
    return searchTag("jdbc-mode-db-pwd");
  }

  /**
   * Returns the path to the file containing the mappings
   **/
  public String mappingsFile() {
    return searchTag("mappings-file");
  }

  /**
   * Returns the path to the owl file
   **/
  public String owlFile() {
    return searchTag("owl-file");
  }

  /**
   * Returns the path to the directory containing the query templates
   **/
  public String queriesDir() {
    return searchTag("queries-dir");
  }

  /**
   * Returns the path to the log file
   **/
  public String logFile() {
    return searchTag("log-file");
  }

  public String logImport() {
    return searchTag("log-import");
  }

  public String logImportFilter() {
    return searchTag("log-import-filter");
  }

  public String logImportPrefix() {
    return searchTag("log-import-prefix");
  }

  public String javaAPIClass() {
    return searchTag("java-api-class");
  }

  /**
   * @return Path for OBDA System Executable (only in Shell mode)
   */
  public String shellCmd() {
    return searchTag("shell-cmd");
  }

  /**
   * @return Specify what to do with the output of the shell command
   */
  public String shellOut() {
    return searchTag("shell-out");
  }

  /**
   * @return Templates for which we force a timeout
   */
  public String forceTimeouts() {
    return searchTag("forced-timeouts");
  }

  /**
   * @return Templates for which we force a timeout
   */
  public String forcedTimeoutsValue() {
    return searchTag("forced-timeouts-timeout-value");
  }

  /**
   * @return
   */
  public String numRuns() {
    return searchTag("num-runs");
  }

  /**
   * @return
   */
  public String numWarmUps() {
    return searchTag("num-warmups");
  }

  /**
   * @return
   */
  public String timeout() {
    return searchTag("timeout");
  }

  public String numClients() {
    return searchTag("num-clients");
  }

  public String rewriting() {
    return searchTag("rewriting");
  }

  public String lang() {
    return searchTag("lang");
  }

  public String mode() {
    return searchTag("mode");
  }

  public String serviceUrl() {
    return searchTag("service-url");
  }

  public String propertiesFile() {
    return searchTag("properties-file");
  }

  protected String searchTag(String tag) {
    try (BufferedReader in = new BufferedReader(
            new FileReader(confFile))) {
      String s;
      while ((s = in.readLine()) != null) {
        List<String> s2 = Arrays.asList(s.split("\\s+"));
        if (s2.get(0).equals(tag)) {
          in.close();
          StringBuilder resultBuilder = new StringBuilder();
          for (int i = 1; i < s2.size(); ++i) {
            if (s2.get(i).startsWith("#")) break; // Comment
            if (i > 1) resultBuilder.append(" ");
            resultBuilder.append(s2.get(i));
          }
          return resultBuilder.toString();
        }
      }
      in.close();
    } catch (IOException e) {
      String msg = "I could not find the "
              + "configuration file, currently set at \"" + confFile + "\". To specify "
              + "another path, use the --conf option.";
      MixerMain.closeEverything(msg, e);
    }
    return "";
  }
}
