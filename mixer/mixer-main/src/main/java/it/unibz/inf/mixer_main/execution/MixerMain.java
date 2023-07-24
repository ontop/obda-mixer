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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_interface.core.Mixers;
import it.unibz.inf.mixer_interface.core.QueryLanguage;
import it.unibz.inf.mixer_main.statistics.StatisticsCollector;
import it.unibz.inf.mixer_main.statistics.StatisticsManager;
import it.unibz.inf.mixer_main.statistics.StatisticsScope;
import it.unibz.inf.mixer_main.utils.Option;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MixerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixerMain.class);

    // Mixer type and config
    private final String mixerType = MixerOptions.optMode.getValue();
    @SuppressWarnings("ConstantConditions")
    private final Map<String, String> mixerConfig = Option.list().stream()
            .filter(o -> o.getName().startsWith("--" + MixerOptions.optMode.getValue()))
            .collect(Collectors.toMap(
                    o -> o.getConfigKey().substring(MixerOptions.optMode.getValue().length() + 1),
                    o -> o.getValue() == null ? null : o.getValue().toString()));

    // Query templates
    private final String templatesDir = MixerOptions.optQueriesDir.getValue();
    private final QueryLanguage language = QueryLanguage.valueOf(MixerOptions.optLang.getValue().toUpperCase());

    // Database access
    private final String databaseUrl = MixerOptions.optDbUrl.getValue();
    private final @Nullable String databaseUser = MixerOptions.optDbUsername.getValue();
    private final @Nullable String databasePwd = MixerOptions.optDbPassword.getValue();
    private final @Nullable String databaseDriverClass = MixerOptions.optDbDriverClass.getValue();

    // Execution settings
    private final int numClients = MixerOptions.optNumClients.getValue();
    private final int numWarmUps = MixerOptions.optNumWarmUps.getValue();
    private final int numRuns = MixerOptions.optNumRuns.getValue();
    private final int timeout = MixerOptions.optTimeout.getValue();
    private final @Nullable String forcedTimeouts = MixerOptions.optForceTimeouts.getValue();

    // Log file handling
    private final String logFile = MixerOptions.optLogFile.getValue();
    private final @Nullable String logImport = MixerOptions.optLogImport.getValue();
    private final @Nullable String logImportFilter = MixerOptions.optLogImportFilter.getValue();
    private final @Nullable String logImportPrefix = MixerOptions.optLogImportPrefix.getValue();

    private void run() {

        // Track exceptions during execution (more than one as we always try to inject and write statistics)
        List<Throwable> exceptions = Lists.newArrayList();

        // Initialize the StatisticsManager keeping statistics in memory
        StatisticsManager statsMgr = new StatisticsManager();

        // Report process marker used to annotate queries in the global scope of statistics
        StatisticsCollector globalStats = statsMgr.getCollector(StatisticsScope.global());
        globalStats.add("marker", StatisticsScope.processMarker());

        try {
            // Instantiate mixer plugin and ensure to close it after use
            try (Mixer mixer = Mixers.create(mixerType)) {

                // Initialize/load the mixer object, tracking loading time
                try {
                    long ts = System.nanoTime();
                    mixer.init(mixerConfig);
                    globalStats.add("load-time", (System.nanoTime() - ts) / 1000000L);
                } catch (Throwable ex) {
                    throw new RuntimeException("Mixer initialization failed", ex);
                }

                // Initialize test threads
                List<MixerThread> threads = new ArrayList<>();
                for (int i = 0; i < numClients; ++i) {
                    TemplateQuerySelector tqs = new TemplateQuerySelector(templatesDir, connect(), language);
                    MixerThread mT = new MixerThread(mixer, tqs, statsMgr, i, numWarmUps, numRuns, timeout,
                            forcedTimeouts == null ? null : Arrays.asList(forcedTimeouts.split("\\s+")));
                    threads.add(mT);
                }

                // Start test threads
                for (MixerThread mT : threads) {
                    mT.start();
                }

                // Wait for test threads termination, forcing it in case of external interruption request
                while (true) {
                    try {
                        for (MixerThread mT : threads) {
                            mT.join();
                        }
                        break;
                    } catch (InterruptedException ex) {
                        exceptions.add(new RuntimeException("Test execution interrupted", ex));
                        for (MixerThread mT : threads) {
                            mT.interrupt();
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            exceptions.add(ex);
        }

        // Import statistics from an external log file, if specified (e.g., the one of the OBDA system)
        if (!Strings.isNullOrEmpty(logImport)) {
            try (Reader in = Files.newBufferedReader(Paths.get(logImport))) {
                Pattern filter = Strings.isNullOrEmpty(logImportFilter) ? null : Pattern.compile(logImportFilter);
                statsMgr.importJson(in, filter, logImportPrefix);
            } catch (Throwable ex) {
                exceptions.add(new RuntimeException("Cannot import statistics from " + logImport, ex));
            }
        }

        // Write statistics
        try {
            statsMgr.write(Paths.get(logFile));
        } catch (Throwable ex) {
            exceptions.add(new RuntimeException("Cannot write log file " + logFile, ex));
        }

        // If one or more exceptions were collected, throw a runtime exception reporting their number and list
        if (!exceptions.isEmpty()) {
            RuntimeException ex = new RuntimeException("Test execution completed with " + exceptions.size() + " errors");
            exceptions.forEach(ex::addSuppressed);
            throw ex;
        }
    }

    private Connection connect() {
        try {
            if (databaseDriverClass != null) {
                Class.forName(databaseDriverClass); // Load driver (might be needed for old drivers)
            }
            return DriverManager.getConnection(databaseUrl, databaseUser, databasePwd);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to instantiate DB connection", ex);
        }
    }

    public static void main(String[] args) {
        try {
            // Process command line arguments and configuration file (arg. values available from MixerOptions)
            MixerOptions.parse(args);

            // Attempt execution
            new MixerMain().run();

            // On success, terminate with exit code 0
            System.exit(0);

        } catch (Throwable ex) {
            // On failure, log error and terminate with exit code 1
            LOGGER.error(ex.getMessage(), ex);
            System.exit(1);
        }
    }

}

