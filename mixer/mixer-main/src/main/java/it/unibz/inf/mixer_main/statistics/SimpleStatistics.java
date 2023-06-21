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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class SimpleStatistics {

    private final Map<String, Integer> mIntegerStats = new HashMap<>();
    private final Map<String, Float> mFloatStats = new HashMap<>();
    private final Map<String, Long> mTimeStats = new HashMap<>();
    private final Map<String, Boolean> mBools = new HashMap<>();

    private final String label;

    public SimpleStatistics(String label) {
        this.label = label;
    }

    public Boolean getBoolean(String key, boolean bool) {
        return mBools.get(key);
    }

    public void setBoolean(String key, boolean bool) {
        mBools.put(key, bool);
    }

    public Long getTime(String key) {
        return mTimeStats.get(key);
    }

    public void setTime(String key, long time) {
        mTimeStats.put(key, time);
    }

    public void addTime(String key, long increment) {
        mTimeStats.merge(key, increment, Long::sum);
    }

    public Integer getInt(String key) {
        return mIntegerStats.get(key);
    }

    public void setInt(String key, int value) {
        mIntegerStats.put(key, value);
    }

    public void addInt(String key, int increment) {
        mIntegerStats.merge(key, increment, Integer::sum);
    }

    public Float getFloat(String key) {
        return mFloatStats.get(key);
    }

    public void setFloat(String key, float value) {
        mFloatStats.put(key, value);
    }

    public void addFloat(String key, float increment) {
        mFloatStats.merge(key, increment, Float::sum);
    }

    public String printStats() {
        try {
            return printStats(new StringBuilder()).toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T extends Appendable> T printStats(T out) throws IOException {
        for (Map<String, ?> stats : Arrays.asList(mIntegerStats, mFloatStats, mTimeStats, mBools)) {
            for (String key : stats.keySet()) {
                out.append("[");
                out.append(label);
                out.append("] [");
                out.append(key);
                out.append("] = ");
                out.append(Objects.toString(stats.get(key)));
                out.append("\n");
            }
        }
        return out;
    }

    public void reset() {
        mFloatStats.clear();
        mIntegerStats.clear();
        mTimeStats.clear();
        mBools.clear();
    }

}
