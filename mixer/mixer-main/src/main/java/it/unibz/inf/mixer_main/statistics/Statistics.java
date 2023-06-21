package it.unibz.inf.mixer_main.statistics;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);

    private final Map<String, SimpleStatistics> mStats = new HashMap<>();

    private final String curLabel;

    public Statistics(String curLabel) {
        this.curLabel = curLabel;
    }

    public String getLabel() {
        return curLabel;
    }

    public SimpleStatistics getSimpleStatsInstance(String label) {
        return mStats.computeIfAbsent(label, SimpleStatistics::new);
    }

    public String printStats() {
        try {
            return printStats(new StringBuilder()).toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T extends Appendable> T printStats(T out) throws IOException {
        out.append("[").append(this.getLabel()).append("]").append("\n"); // Thread-number
        for (String label : mStats.keySet()) {
            mStats.get(label).printStats(out);
        }
        return out;
    }

    @SuppressWarnings("unused")
    public void reset() {
        mStats.clear();
        System.gc();
    }

    @SuppressWarnings("unused")
    public void merge(Statistics toMerge) {
        for (String label : toMerge.mStats.keySet()) {
            if (mStats.containsKey(label)) {
                LOGGER.warn("Unmergeable statistics for label = {}", label);
            } else {
                mStats.put(label, toMerge.mStats.get(label));
            }
        }
    }

}