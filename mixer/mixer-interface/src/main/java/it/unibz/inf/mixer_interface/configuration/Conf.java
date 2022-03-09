package it.unibz.inf.mixer_interface.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  private final String propertyFile;
  protected String forcedTimeouts;
  protected int forcedTimeoutsTimeoutValue;

  // java-api-mode
  protected final String javaApiClass;
  protected int numRuns;
  protected int numWarmUps;
  protected int timeout;
  protected int numClients;
  protected boolean rewriting;

  // modes
  protected String mode;
  // shell-mode
  protected String shellCmd;
  protected boolean shellOutput;
  // web-mode
  protected String serviceUrl;
  // jdbc-mode
  protected final String jdbcModeDriverClass;
  protected final String jdbcModeDatabaseUrl;
  protected final String jdbcModeDatabaseUser;
  protected final String jdbcModeDatabasePwd;

  public String getJdbcModeDriverClass() {
    return jdbcModeDriverClass;
  }

  public String getJdbcModeDatabaseUrl() {
    return jdbcModeDatabaseUrl;
  }

  public String getJdbcModeDatabaseUser() {
    return jdbcModeDatabaseUser;
  }

  public String getJdbcModeDatabasePwd() {
    return jdbcModeDatabasePwd;
  }

  public Conf(
          int numRuns,
          int numWarmUps,
          int timeout,
          int numClients,
          boolean rewriting,
          String mode,
          String serviceUrl,
          String owlFile,
          String mappingsFile,
          String propertyFile,
          String driverClass,
          String databaseUrl,
          String databaseUser,
          String databasePwd,
          String logFile,
          String templatesDir,
          String shellCmd,
          boolean shellOutput,
          String forcedTimeouts,
          int forcedTimeoutsValue,
          String javaApiClass,
          String jdbcModeDriverClass, String jdbcModeDatabaseUrl, String jdbcModeDatabaseUser, String jdbcModeDatabasePwd) {
    this.numRuns = numRuns;
    this.numWarmUps = numWarmUps;
    this.timeout = timeout;
    this.numClients = numClients;
    this.rewriting = rewriting;
    this.mode = mode;
    this.serviceUrl = serviceUrl;
    this.owlFile = owlFile;
    this.mappingsFile = mappingsFile;
    this.propertyFile = propertyFile;
    this.driverClass = driverClass;
    this.databaseUrl = databaseUrl;
    this.databaseUser = databaseUser;
    this.databasePwd = databasePwd;
    this.logFile = logFile;
    this.templatesDir = templatesDir;
    this.shellCmd = shellCmd;
    this.shellOutput = shellOutput;
    this.forcedTimeouts = forcedTimeouts;
    this.forcedTimeoutsTimeoutValue = forcedTimeoutsValue;
    this.javaApiClass = javaApiClass;
    this.jdbcModeDriverClass = jdbcModeDriverClass;
    this.jdbcModeDatabaseUrl = jdbcModeDatabaseUrl;
    this.jdbcModeDatabaseUser = jdbcModeDatabaseUser;
    this.jdbcModeDatabasePwd = jdbcModeDatabasePwd;
  }


  public String getJavaApiClass() {
    return javaApiClass;
  }

  public int getNumRuns() {
    return numRuns;
  }

  public int getNumWarmUps() {
    return numWarmUps;
  }

  public int getTimeout() {
    return timeout;
  }

  public int getNumClients() {
    return numClients;
  }

  public boolean rewriting() {
    return rewriting;
  }

  public String getMode() {
    return mode;
  }

  public String getServiceUrl() {
    return serviceUrl;
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

  public String getShellCmd() {
    return this.shellCmd;
  }

  public boolean getShellOutput() {
    return this.shellOutput;
  }

  public List<String> getForcedTimeouts() {

    if (this.forcedTimeouts.equals("null")) return new ArrayList<>();

    List<String> splits = Arrays.asList(forcedTimeouts.split("\\s+"));
    List<String> result = new ArrayList<>();
    for (String split : splits) {
      result.add(split);
    }
    return result;
  }

  public int getForcedTimeoutsTimeoutValue() {
    return this.forcedTimeoutsTimeoutValue;
  }

  public String getJavaAPIClass() {
    return this.javaApiClass;
  }

  public String getPropertyFile() {
    return this.propertyFile;
  }
}
