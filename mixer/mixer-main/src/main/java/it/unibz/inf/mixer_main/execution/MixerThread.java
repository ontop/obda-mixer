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
import java.sql.SQLException;
import java.util.UUID;

import it.unibz.inf.mixer_db_connection.*;
import it.unibz.inf.mixer_main.statistics.StatisticsCollector;
import it.unibz.inf.mixer_main.statistics.StatisticsManager;
import it.unibz.inf.mixer_main.statistics.StatisticsScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.time.Chrono;

public class MixerThread extends Thread {

  static Logger log = LoggerFactory.getLogger(MixerThread.class);

  // Logging
  private final Mixer mixer;
  private final StatisticsManager statsMgr;
  private final int client;

  private final int nRuns; // Number of total runs
  private final int nWUps; // Number of warm-up runs
  private final int timeout; // timeout time

  // Do I (me, thread) have to collect rewriting and unfolding time?
  final boolean rwAndUnf = true; // TODO Remove

  // Time statistics
  private final Chrono chrono;
  private final Chrono chronoMix;

  public MixerThread(Mixer m, StatisticsManager statMgr, File[] listOfFiles, int client) {
    this.mixer = m;
    this.statsMgr = statMgr;
    this.client = client;
    this.nRuns = m.getConfiguration().getNumRuns();
    this.nWUps = m.getConfiguration().getNumWarmUps();
    this.timeout = m.getConfiguration().getTimeout();

    chrono = new Chrono();
    chronoMix = new Chrono();

    //		this.rwAndUnf = rwAndUnf;
  }

  public void setUp() {
    // What shall I do here?
  }

  public void run() {

    // Establish the connection
    Conf conf = mixer.getConfiguration();
    String driver = conf.getDriverClass();

    DBMSConnection db = null;
    try{
      switch(driver) {
        case DBType.MYSQL:
          db = new DBMSConnectionMysql(conf.getDatabaseUrl(), conf.getDatabaseUser(), conf.getDatabasePwd(), conf.getDriverClass());
          break;
        case DBType.POSTGRES:
          db = new DBMSConnectionPostgres(conf.getDatabaseUrl(), conf.getDatabaseUser(), conf.getDatabasePwd(), conf.getDriverClass());
          break;
        case DBType.SQLSERVER:
          db = new DBMSConnectionMSSQL(conf.getDatabaseUrl(), conf.getDatabaseUser(), conf.getDatabasePwd(), conf.getDriverClass());
          break;
        case DBType.DB2:
          db = new DBMSConnectionDB2(conf.getDatabaseUrl(), conf.getDatabaseUser(), conf.getDatabasePwd(), conf.getDriverClass());
          break;
        case DBType.TEIID:
          db = new DBMSConnectionTeiid(conf.getDatabaseUrl(), conf.getDatabaseUser(), conf.getDatabasePwd(), conf.getDriverClass());
          break;
      }
    } catch (SQLException | ClassNotFoundException e) {
      MixerMain.closeEverything("Falied to instantiate DB connection", e);
    }

    TemplateQuerySelector tqs = new TemplateQuerySelector(conf, db);

    // Warm up
    warmUp(tqs);

    // The actual tests
    test(tqs);
  }

  /**
   * It performs the mixes, and collects the statistics
   *
   * @param tqs
   */
  private void test(TemplateQuerySelector tqs) {

    for (int mix = 0; mix < nRuns; ++mix) {
      long forcedTimeoutsSum = 0;
      long timeWasted = 0;
      chronoMix.start();
      //			 int i = 0;
      StatisticsCollector mixStats = statsMgr.getCollector(StatisticsScope.forMix(client, mix));
      while (true) {
        chrono.start();
        String query = tqs.getNextQuery();
        if (query == null) {
          timeWasted += chrono.stop();
          break;
        }
        StatisticsScope queryScope = StatisticsScope.forQuery(client, mix, tqs.getCurQueryName());
        StatisticsCollector queryStats = statsMgr.getCollector(queryScope);
        String queryWithScope = encodeScopeAsComment(query, queryScope);
        log.info("Test query:\n{}", queryWithScope);
        timeWasted += chrono.stop();
        if (query.equals("force-timeout")) {
          int forcedTimeout = mixer.getConfiguration().getForcedTimeoutsTimeoutValue();
          queryStats.add("execution_time", forcedTimeout * 1000L); // Convert to milliseconds
          forcedTimeoutsSum += forcedTimeout * 1000L; // Convert to milliseconds
        } else {
          chrono.start();
          Object resultSet = mixer.executeQuery(queryWithScope, timeout);
          queryStats.add("execution_time", chrono.stop());
          chrono.start();
          int numResults = mixer.traverseResultSet(resultSet);
          queryStats.add("resultset_traversal_time", chrono.stop());
          queryStats.add("num_results", numResults);
          chrono.start();
          if (this.rwAndUnf) {
            queryStats.add("rewriting_time", mixer.getRewritingTime());
            queryStats.add("unfolding_time", mixer.getUnfoldingTime());

            // Get query statistics
            if (this.nRuns == 1 && this.nWUps == 0) {
              queryStats.set("rewritingUCQ_size", mixer.getRewritingSize());
              queryStats.set("unfoldingUCQ_size", mixer.getUnfoldingSize());
            }
          }
          timeWasted += chrono.stop();
        }
      }
      // mix time
      mixStats.add("mix_time", chronoMix.stop() - timeWasted + forcedTimeoutsSum);
    }
  }

  private void warmUp(TemplateQuerySelector tqs) {
    for (int j = 0; j < nWUps; ++j) {
      boolean stop = false;
      while (!stop) {
        String query = tqs.getNextQuery();
        if (query == null) {
          stop = true;
        } else {
          StatisticsScope scope = StatisticsScope.forQuery(client, -nWUps + j, tqs.getCurQueryName());
          String queryWithScope = encodeScopeAsComment(query, scope);
          log.debug("Warm-up query:\n{}", queryWithScope);
          if (!query.equals("force-timeout")) {
            mixer.executeWarmUpQuery(queryWithScope, timeout);
          }
        }
      }
    }
  }

  private String encodeScopeAsComment(String query, StatisticsScope scope) {
    if ("sql".equalsIgnoreCase(mixer.getConfiguration().getLang())) {
      return "-- " + scope + "\n" + query;
    } else {
      return "# " + scope + "\n" + query;
    }
  }

}