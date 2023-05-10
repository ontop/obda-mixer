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

import it.unibz.inf.mixer_db_connection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_main.statistics.SimpleStatistics;
import it.unibz.inf.mixer_main.statistics.Statistics;
import it.unibz.inf.mixer_main.time.Chrono;

public class MixerThread extends Thread {

  static Logger log = LoggerFactory.getLogger(MixerThread.class);

  // Logging
  private final Statistics stat;
  private final Mixer mixer;

  private final int nRuns; // Number of total runs
  private final int nWUps; // Number of warm-up runs
  private final int timeout; // timeout time

  // Do I (me, thread) have to collect rewriting and unfolding time?
  final boolean rwAndUnf = true; // TODO Remove

  // Time statistics
  private final Chrono chrono;
  private final Chrono chronoMix;

  public MixerThread(Mixer m, Statistics stat, File[] listOfFiles) {
    this.stat = stat;
    this.mixer = m;
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

    for (int j = 0; j < nRuns; ++j) {
      long forcedTimeoutsSum = 0;
      long timeWasted = 0;
      chronoMix.start();
      //			 int i = 0;
      SimpleStatistics localStat = stat.getSimpleStatsInstance("run#" + j);
      boolean stop = false;
      while (!stop) {
        chrono.start();
        String query = tqs.getNextQuery();
        log.info(query);
        System.out.println(query);

        timeWasted += chrono.stop();
        if (query == null) {
          stop = true;
        } else if (query.equals("force-timeout")) {
          int forcedTimeout = mixer.getConfiguration().getForcedTimeoutsTimeoutValue();
          localStat.addTime("execution_time#" + tqs.getCurQueryName(), forcedTimeout * 1000L); // Convert to milliseconds
          forcedTimeoutsSum += forcedTimeout * 1000L; // Convert to milliseconds
        } else {
          Object resultSet = null;
          chrono.start();

          if (timeout == 0) resultSet = mixer.executeQuery(query);
          else resultSet = mixer.executeQuery(query, timeout);
          localStat.addTime("execution_time#" + tqs.getCurQueryName(), chrono.stop());
          chrono.start();
          int numResults = mixer.traverseResultSet(resultSet);
          localStat.addTime("resultset_traversal_time#" + tqs.getCurQueryName(), chrono.stop());
          localStat.addInt("num_results#" + tqs.getCurQueryName(), numResults);
          chrono.start();
          if (this.rwAndUnf) {
            localStat.addTime("rewriting_time#" + tqs.getCurQueryName(), mixer.getRewritingTime());
            localStat.addTime("unfolding_time#" + tqs.getCurQueryName(), mixer.getUnfoldingTime());

            // Get query statistics
            if (this.nRuns == 1 && this.nWUps == 0) {
              localStat.setInt("rewritingUCQ_size#" + tqs.getCurQueryName(), mixer.getRewritingSize());
              localStat.setInt("unfoldingUCQ_size#" + tqs.getCurQueryName(), mixer.getUnfoldingSize());
            }
          }
          timeWasted += chrono.stop();
        }
      }
      // mix time
      localStat.addTime("mix_time#" + j, chronoMix.stop() - timeWasted + forcedTimeoutsSum);
    }
  }

  private void warmUp(TemplateQuerySelector tqs) {
    for (int j = 0; j < nWUps; ++j) {
      boolean stop = false;
      while (!stop) {
        String query = tqs.getNextQuery();
        log.debug(query);
        if (query == null) {
          stop = true;
        } else {
          if (!query.equals("force-timeout")) {
            if (timeout == 0) mixer.executeWarmUpQuery(query);
            else mixer.executeWarmUpQuery(query, timeout);
          }
        }
      }
    }
  }
}