package it.unibz.inf.mixer.execution;

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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unibz.inf.mixer.core.Mixer;
import it.unibz.inf.mixer.core.Plugins;
import it.unibz.inf.mixer.core.QuerySelector;
import it.unibz.inf.mixer.execution.statistics.StatisticsCollector;
import it.unibz.inf.mixer.execution.statistics.StatisticsManager;
import it.unibz.inf.mixer.execution.statistics.StatisticsScope;
import it.unibz.inf.mixer.execution.utils.Option;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MixerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixerMain.class);

    // Query selector name and config
    private final String selectorName = MixerOptions.optSelector.getValue();
    private final Map<String, String> selectorConfig = Option.list().stream()
            .filter(o -> o.getValue() != null && o.getName().startsWith("--" + MixerOptions.optSelector.getValue()))
            .collect(ImmutableMap.toImmutableMap(
                    o -> o.getConfigKey().substring(MixerOptions.optSelector.getValue().length() + 1),
                    o -> o.getValue().toString()));

    // Mixer name and config
    private final String mixerName = MixerOptions.optMixer.getValue();
    private final Map<String, String> mixerConfig = Option.list().stream()
            .filter(o -> o.getValue() != null && o.getName().startsWith("--" + MixerOptions.optMixer.getValue()))
            .collect(ImmutableMap.toImmutableMap(
                    o -> o.getConfigKey().substring(MixerOptions.optMixer.getValue().length() + 1),
                    o -> o.getValue().toString()));

    // Execution settings
    private final int numClients = MixerOptions.optNumClients.getValue();
    private final int numRuns = MixerOptions.optNumRuns.getValue();
    private final int numWarmUps = MixerOptions.optNumWarmUps.getValue();
    private final int timeWarmUps = MixerOptions.optTimeWarmUps.getValue();
    private final int separation = MixerOptions.optSeparation.getValue();
    private final int separationWarmUps= MoreObjects.firstNonNull(MixerOptions.optSeparationWarmUps.getValue(), separation);
    private final int timeout = MixerOptions.optTimeout.getValue();
    private final int timeoutWarmUps = MoreObjects.firstNonNull(MixerOptions.optTimeoutWarmUps.getValue(), timeout);
    private final @Nullable String forcedTimeouts = MixerOptions.optForceTimeouts.getValue();
    private final int retryAttempts = MixerOptions.optRetryAttempts.getValue();
    private final int retryWaitTime = MixerOptions.optRetryWaitTime.getValue();
    private final Pattern retryCondition = Pattern.compile(MixerOptions.optRetryCondition.getValue());

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

        // Report configuration and process marker used to annotate queries in the global scope of statistics
        StatisticsCollector globalStats = statsMgr.getCollector(StatisticsScope.global());
        globalStats.add("marker", StatisticsScope.processMarker());
        globalStats.set("settings", Option.list().stream()
                .filter(o -> o.getValue() != null)
                .filter(o -> "EXECUTION".equals(o.getCategory()) || "LOGGING".equals(o.getCategory())
                        || o.getConfigKey().startsWith(MixerOptions.optMixer.getValue())
                        || o.getConfigKey().startsWith(MixerOptions.optSelector.getValue()))
                .collect(ImmutableMap.toImmutableMap(Option::getConfigKey, Option::getValue)));


        // Define variables for query selector and mixer, whose lifecycle is jointly managed next
        QuerySelector selector = null;
        Mixer mixer = null;

        try {
            try {
                // Initialize/load the query selector object
                selector = (QuerySelector) Plugins.create(selectorName);
                selector.init(selectorConfig);

                // Initialize/load the mixer object, tracking loading time
                mixer = (Mixer) Plugins.create(mixerName);
                long ts = System.nanoTime();
                mixer.init(mixerConfig);
                globalStats.add("load-time", (System.nanoTime() - ts) / 1000000L);

                // Initialize test threads
                List<MixerThread> threads = new ArrayList<>();
                for (int i = 0; i < numClients; ++i) {
                    MixerThread thread = new MixerThread(mixer, selector, statsMgr, i,
                            numRuns, numWarmUps, timeWarmUps, separation, separationWarmUps, timeout, timeoutWarmUps,
                            forcedTimeouts == null ? null : Arrays.asList(forcedTimeouts.split("\\s+")),
                            retryAttempts, retryWaitTime, retryCondition);
                    threads.add(thread);
                }

                // Start test threads
                for (MixerThread thread : threads) {
                    thread.start();
                }

                // Wait for test threads termination, forcing it in case of external interruption request
                while (true) {
                    try {
                        for (MixerThread thread : threads) {
                            thread.join();
                        }
                        break;
                    } catch (InterruptedException ex) {
                        exceptions.add(new RuntimeException("Test execution interrupted", ex));
                        for (MixerThread mT : threads) {
                            mT.interrupt();
                        }
                    }
                }
            } finally {
                // Close mixer and query selector, if created and initialized before
                closeQuietly(mixer);
                closeQuietly(selector);
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

    private static void closeQuietly(@Nullable Object object) {
        if (object instanceof AutoCloseable) {
            try {
                ((AutoCloseable) object).close();
            } catch (Throwable ex) {
                LOGGER.warn("Error while closing " + object.getClass().getSimpleName(), ex);
            }
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

