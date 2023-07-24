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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import it.unibz.inf.mixer_interface.core.Handler;
import it.unibz.inf.mixer_interface.core.Handlers;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_interface.core.Query;
import it.unibz.inf.mixer_main.statistics.StatisticsCollector;
import it.unibz.inf.mixer_main.statistics.StatisticsManager;
import it.unibz.inf.mixer_main.statistics.StatisticsScope;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public final class MixerThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixerThread.class);

    private final Mixer mixer;

    private final TemplateQuerySelector tqs;

    private final StatisticsManager statsMgr;

    private final int clientId;

    private final int numWarmUps;

    private final int numRuns;

    private final int timeout;

    private final Set<String> forceTimeoutQueries; // Queries to skip, treating them as timed out

    public MixerThread(Mixer mixer, TemplateQuerySelector tqs, StatisticsManager statMgr, int clientId,
                       int numWarmUps, int numRuns, int timeout, @Nullable Iterable<String> forceTimeoutQueries) {

        // Check arguments
        Objects.requireNonNull(mixer);
        Objects.requireNonNull(tqs);
        Objects.requireNonNull(statMgr);
        Preconditions.checkArgument(clientId >= 0);
        Preconditions.checkArgument(numWarmUps >= 0);
        Preconditions.checkArgument(numRuns >= 0);
        Preconditions.checkArgument(timeout >= 0);

        // Initialize state
        this.mixer = mixer;
        this.tqs = tqs;
        this.statsMgr = statMgr;
        this.clientId = clientId;
        this.numWarmUps = numWarmUps;
        this.numRuns = numRuns;
        this.timeout = timeout;
        this.forceTimeoutQueries = forceTimeoutQueries != null
                ? ImmutableSet.copyOf(forceTimeoutQueries)
                : ImmutableSet.of();
    }

    public void run() {
        warmUp();
        test();
    }

    private void warmUp() {

        // Keep a <query_scope, query_string> map of queries whose execution failed, to report it in the statistics
        Map<String, String> failedQueries = new HashMap<>();

        // Iterate over warm up query mixes (by their index, 0 to numWarmUps excluded)
        for (int j = 0; j < numWarmUps; ++j) {

            // Iterate over the queries in the current query mix
            while (true) {

                // Fetch the current query, which is null if the query mix is completed
                Query query = tqs.nextQuery();
                if (query == null) {
                    break;
                }

                // Retrieve the <clientId, mixId, queryId> query scope
                StatisticsScope queryScope = StatisticsScope.forQuery(clientId, -numWarmUps + j, query.getId());

                // Edit the query to set timeout, mark results as ignored, and include scope as query string comment
                Query queryWithScope = query.toBuilder()
                        .withString(query.getLanguage().getCommentString() + " " + queryScope + "\n" + query.getString())
                        .withTimeout(timeout)
                        .withResultIgnored(true)
                        .build();

                // Skip queries configured to be marked as timed out (they are not going to be tested either)
                if (this.forceTimeoutQueries.contains(query.getId())) {
                    continue;
                }

                // Otherwise, log the query going to be executed
                LOGGER.debug("Warm-up query:\n{}", queryWithScope);

                try {
                    // Run the query and update statistics
                    mixer.execute(queryWithScope, Handlers.nil());
                } catch (Throwable ex) {
                    // On failure, keep track of failed query so to report it in statistics and log the issue
                    failedQueries.put(queryScope.toString(), queryWithScope.toString(true));
                    LOGGER.warn("Warm up query execution failed: " + ex.getMessage() + "\n" + queryWithScope, ex);
                }
            }
        }

        // Report failed warm up queries, if any
        if (!failedQueries.isEmpty()) {
            statsMgr.getCollector(StatisticsScope.forClient(clientId)).set("failed_warm_up_queries", failedQueries);
        }
    }

    private void test() {

        // Retrieve global and client-level statistics collectors
        StatisticsCollector globalStats = statsMgr.getCollector(StatisticsScope.global());
        StatisticsCollector clientStats = statsMgr.getCollector(StatisticsScope.forClient(clientId));

        // Keep a <query_scope, query_string> map of queries whose execution failed, to report it in the statistics
        Map<String, String> failedQueries = new HashMap<>();

        // Iterate over test query mixes (by their index, 0 to numRuns excluded)
        for (int mix = 0; mix < numRuns; ++mix) {

            // Retrieve the <clientId, mixId> mix scope and associated collector, and track mix total execution time
            StatisticsScope mixScope = StatisticsScope.forMix(clientId, mix);
            StatisticsCollector mixStats = statsMgr.getCollector(mixScope);

            // Iterate over the queries in the current query mix
            while (true) {

                // Fetch the current query, which is null if the query mix is completed
                Query query = tqs.nextQuery();
                if (query == null) {
                    break;
                }

                // Retrieve <clientId, mixId, queryId> query scope and associated statistics collector
                StatisticsScope queryScope = StatisticsScope.forQuery(clientId, mix, query.getId());
                StatisticsCollector queryStats = statsMgr.getCollector(queryScope);

                // Store query string hash in the statistics (for comparisons) prior to adding comments to it
                queryStats.set("placeholders", query.getPlaceholders());
                queryStats.set("sorted", query.isResultSorted());
                queryStats.set("hash_input", Long.toString(Math.abs(Hashing.farmHashFingerprint64().newHasher()
                        .putUnencodedChars(query.getString()).hash()
                        .asLong()), 36));

                // Edit query to set timeout and include scope as query string comment (to intercept it in server logs)
                Query queryWithScope = query.toBuilder()
                        .withString(query.getLanguage().getCommentString() + " " + queryScope + "\n" + query.getString())
                        .withTimeout(timeout)
                        .build();

                // Skip queries configured to be marked as timed out, using the timeout as their execution time
                if (this.forceTimeoutQueries.contains(queryWithScope.getId())) {
                    long forcedTimeoutMs = timeout * 1000L; // convert to ms
                    queryStats.add("execution_time", forcedTimeoutMs);
                    continue;
                }

                // Otherwise, log query going to be executed
                LOGGER.info("Test query:\n{}", queryWithScope);

                // Run the query and update statistics
                QueryExecutionHandler handler = new QueryExecutionHandler(queryWithScope.isResultSorted());
                Throwable exception = null;
                try {
                    mixer.execute(queryWithScope, handler);
                } catch (Throwable ex) {
                    failedQueries.put(queryScope.toString(), queryWithScope.toString(true));
                    LOGGER.warn("Test query execution failed: " + ex.getMessage() + "\n" + queryWithScope, ex);
                    exception = ex;
                }
                handler.complete(queryStats, exception, timeout);

                // Aggregate query statistics into mix statistics
                aggregate(mixStats, queryStats);
            }

            // Aggregate mix statistics into client statistics
            aggregate(clientStats, mixStats);
        }

        // Aggregate client statistics into global statistics
        aggregate(globalStats, clientStats);

        // Report failed queries at global scope
        if (!failedQueries.isEmpty()) {
            globalStats.set("failed_test_queries", failedQueries,
                    (m1, m2) -> ImmutableMap.<String, String>builder().putAll(m1).putAll(m2).build());
        }
    }

    private static void aggregate(StatisticsCollector parent, StatisticsCollector child) {

        // Add times measures in the child scope to the parent scope (execution, resultset traversal, total times)
        for (String a : new String[]{"execution_time", "resultset_traversal_time", "total_time"}) {
            parent.set(a, MoreObjects.firstNonNull(child.get(a, Long.class), 0L), Long::sum);
        }

        // Update input/output hashes at parent scope based on the ones in child scope
        for (String a : new String[]{"hash_input", "hash_output"}) {
            parent.set(a, MoreObjects.firstNonNull(child.get(a, String.class), "0"),
                    (h1, h2) -> Long.toString(Long.parseLong(h1, 36) ^ Long.parseLong(h2, 36), 36));
        }

        // Update outcome statistics at parent scope based on the ones (either 'outcomes' or 'outcome') in child scope
        Map<String, Integer> childOutcomes = child.get("outcomes");
        if (childOutcomes == null) {
            String outcome = child.get("outcome");
            childOutcomes = outcome != null ? ImmutableMap.of(outcome, 1) : ImmutableMap.of();
        }
        parent.set("outcomes", childOutcomes, (o1, o2) ->
                Sets.union(o1.keySet(), o2.keySet()).stream().collect(ImmutableMap.toImmutableMap(o -> o, o ->
                        o1.getOrDefault(o, 0) + o2.getOrDefault(o, 0))));
    }

    private static final class QueryExecutionHandler implements Handler {

        private final boolean resultSorted;

        private final List<Object> metadata = Lists.newArrayListWithCapacity(16);

        private @Nullable Integer numSolutions;

        private int numSolutionsHashed;

        private final @Nullable Hasher sortedSolutionsHasher;

        private long unsortedSolutionsHash;

        private long currentSolutionHash;

        private final long tsCreated = System.nanoTime();

        private long tsSubmit;

        private long tsResults;

        private long tsEnd;

        private QueryExecutionHandler(boolean resultSorted) {
            this.resultSorted = resultSorted;
            this.sortedSolutionsHasher = resultSorted ? Hashing.farmHashFingerprint64().newHasher() : null;
        }

        @Override
        public void onSubmit() {
            tsSubmit = System.nanoTime();
        }

        @Override
        public void onStartResults() {
            tsResults = System.nanoTime();
        }

        @Override
        public void onSolutionIRIBinding(String variable, String iri) {
            currentSolutionHash ^= onSolutionBindingHelper(variable, (char) 1, iri).hash().asLong();
        }

        @Override
        public void onSolutionBNodeBinding(String variable, String id) {
            currentSolutionHash ^= onSolutionBindingHelper(variable, (char) 2, id).hash().asLong();
        }

        @Override
        public void onSolutionLiteralBinding(String variable, String label, @Nullable String datatypeIri, @Nullable String lang) {
            if (datatypeIri == null) {
                datatypeIri = lang == null
                        ? "http://www.w3.org/2001/XMLSchema#string"
                        : "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
            }
            currentSolutionHash ^= onSolutionBindingHelper(variable, (char) 3, label)
                    .putChar((char) 0)
                    .putUnencodedChars(datatypeIri)
                    .putChar((char) 0)
                    .putUnencodedChars(Strings.nullToEmpty(lang))
                    .hash()
                    .asLong();
        }

        @Override
        public void onSolutionSQLBinding(String variable, Object value, int type) {
            currentSolutionHash ^= onSolutionBindingHelper(variable, (char) 3, value.toString())
                    .putChar((char) 0)
                    .putInt(type)
                    .hash()
                    .asLong();
        }

        private Hasher onSolutionBindingHelper(String variable, char valueType, String value) {
            if (tsResults == 0) {
                tsResults = System.nanoTime();
                LOGGER.warn("Handler.onStartResults() not called by Mixer, using first solution binding timestamp");
            }
            return Hashing.farmHashFingerprint64().newHasher()
                    .putChar(valueType)
                    .putUnencodedChars(variable)
                    .putChar((char) 0)
                    .putUnencodedChars(value);
        }

        @Override
        public void onSolutionCompleted() {
            if (resultSorted) {
                sortedSolutionsHasher.putLong(currentSolutionHash);
            } else {
                // Need to transform the hash (XOR of binding hashes) as otherwise we would generate the same hash for
                // a given set of bindings independently of the solutions they belong to
                currentSolutionHash = Hashing.farmHashFingerprint64().newHasher()
                        .putLong(currentSolutionHash)
                        .hash()
                        .asLong();
                unsortedSolutionsHash ^= currentSolutionHash;
                currentSolutionHash = 0L;
            }
            ++numSolutionsHashed;
        }

        @Override
        public void onEndResults(@Nullable Integer numSolutions) {
            tsEnd = System.nanoTime();
            this.numSolutions = numSolutions;
        }

        @Override
        public void onMetadata(String attribute, Object value) {
            // Postpone processing of metadata to minimize overhead
            metadata.add(attribute);
            metadata.add(value);
        }

        public void complete(StatisticsCollector collector, @Nullable Throwable exception, long timeout) {

            // Take current timestamp, to be used in case tsEnd is not available
            long tsCompleted = System.nanoTime();

            // Try to recover tsSubmit and tsResults if we can infer these events should have occurred but Mixer did not
            // call the respective callbacks, reporting warnings (don't recover missing tsEnd, treated as timeout)
            if (tsSubmit == 0 && (tsResults != 0 || tsEnd != 0)) {
                tsSubmit = tsCreated;
                LOGGER.warn("Handler.onSubmit() not called by Mixer but was expected as query results were received, "
                        + "using timestamp of Mixer.execute() call instead");
            }
            if (tsResults == 0 && tsEnd != 0) {
                tsResults = tsEnd;
                LOGGER.warn("Handler.onStartResults() not called by Mixer, using timestamp of Handler.onEndResults() instead");
            }

            // Report query execution outcome (enumerated attribute)
            if (tsSubmit == 0) {
                collector.set("outcome", "error_not_submitted"); // not submitted -> error, even without exception
            } else if (tsResults == 0) {
                collector.set("outcome", exception != null ? "error_no_results" : "timeout_no_results");
            } else if (tsEnd == 0) {
                collector.set("outcome", exception != null ? "error_partial_results" : "timeout_partial_results");
            } else {
                collector.set("outcome", "success"); // all results received -> success, even if there was an exception
            }

            // Report exception class and message, if defined
            if (exception != null) {
                StringWriter w = new StringWriter();
                exception.printStackTrace(new PrintWriter(w));
                collector.set("exception", w.toString());
            }

            // Compute times based on ms timestamps, using tsCreated/tsCompleted when undefined
            long totalTimeMs = ((tsEnd != 0 ? tsEnd : tsCompleted) - (tsSubmit != 0 ? tsSubmit : tsCreated)) / 1000000L;
            long executionTimeMs = ((tsResults != 0 ? tsResults : tsCompleted) - (tsSubmit != 0 ? tsSubmit : tsCreated)) / 1000000L;

            // Adapt times in case of timeout (set totalTime = timeout and ensure executionTime <= totalTime)
            if (tsEnd == 0 && exception == null) {
                long timeoutMs = timeout * 1000;
                if (totalTimeMs < timeoutMs) {
                    LOGGER.warn("Query reported as not completed (i.e., timed out) by Mixer, but configured timeout = "
                            + timeout + "s has not elapsed");
                }
                totalTimeMs = timeoutMs; // use timeout as total time (might have measured a longer time)
                executionTimeMs = executionTimeMs < totalTimeMs && tsResults != 0 ? executionTimeMs : timeout;
            }

            // Store times
            collector.set("execution_time", executionTimeMs);
            collector.set("resultset_traversal_time", (totalTimeMs - executionTimeMs));
            collector.set("total_time", totalTimeMs);

            // If the number of results is known, store it along with results hashes (if possible)
            if (numSolutions != null) {
                collector.set("num_results", numSolutions);
                if (numSolutions == numSolutionsHashed) {
                    collector.set("hash_output", Long.toString(Math.abs(
                            resultSorted ? sortedSolutionsHasher.hash().asLong() : unsortedSolutionsHash), 36));
                }
            }

            // Store additional metadata reported by the Mixer
            for (int i = 0; i < metadata.size(); i += 2) {
                String attribute = (String) metadata.get(i);
                Object value = metadata.get(i + 1);
                collector.set(attribute, value);
            }
        }

    }

}