package it.unibz.inf.mixer_main.utils;

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

import com.google.common.base.Strings;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author tir
 * <p>
 * Constraints:
 * <p>
 * 1) The template string CANNOT start with a placeholder '?'
 * 2) There cannot be two consecutive placeholders --this would not make sense, anyway
 */
@SuppressWarnings("unused")
public final class Template {

    private static final String PLACEHOLDER_PREFIX = "$";

    private final String template;

    private final String[] splits;

    private final String[] fillers;

    public Template(String templateString) {
        template = Objects.requireNonNull(templateString);
        splits = templateString.split("\\" + PLACEHOLDER_PREFIX);
        int cnt = 0;
        for (int i = 0; i < templateString.length(); ++i) {
            if (templateString.charAt(i) == PLACEHOLDER_PREFIX.charAt(0)) {
                cnt++;
            }
        }
        fillers = new String[cnt];
    }

    /**
     * Returns the number of placeholders in the template.
     *
     * @return the number of placeholders
     */
    public int getNumPlaceholders() {
        return fillers.length;
    }

    /**
     * Returns the filler for the placeholder at 1-based index {@code n}.
     *
     * @param n the placeholder index
     * @return the current placeholder filler, possibly null
     */
    public @Nullable String getNthPlaceholder(int n) {
        return fillers[n - 1];
    }

    /**
     * Sets the filler for the placeholder at 1-based index {@code n}.
     *
     * @param n      the placeholder index
     * @param filler the new placeholder filler, possibly null
     */
    public void setNthPlaceholder(int n, @Nullable String filler) {
        fillers[n - 1] = filler;
    }

    /**
     * Returns placeholder definition (index, qualified name, quoting) at specified 1-based index {@code n}.
     *
     * @param n the placeholder index
     * @return the {@code PlaceholderInfo} object for that placeholder
     */
    public PlaceholderInfo getNthPlaceholderInfo(int n) {
        String s = splits[n];
        return new PlaceholderInfo(s.substring(s.indexOf("{"), s.indexOf("}") + 1));
    }

    public String getFilled() {
        StringBuilder sb = new StringBuilder();
        sb.append(splits[0]); // Part before first occurrence of '$'
        if (fillers.length > 0) {
            sb.append(Strings.nullToEmpty(fillers[0]));
            for (int i = 1; i < splits.length; ++i) {
                sb.append(splits[i].substring(splits[i].indexOf("}") + 1));
                if (i < fillers.length) {
                    sb.append(fillers[i]);
                }
            }
        }
        return sb.toString();
    }

    public String toString() {
        return template;
    }

    public enum Quoting {

        NONE,

        UNDERSCORE,

        PERCENT;

        public String apply(String toInsert) {
            switch (this) {
                case NONE:
                    return toInsert;
                case PERCENT:
                    return toInsert.replaceAll(" ", "%20");
                case UNDERSCORE:
                    return toInsert.replaceAll(" ", "_");
                default:
                    throw new IllegalArgumentException("Unsupported quoting " + this);
            }
        }

    }

    @SuppressWarnings("unused")
    public static final class PlaceholderInfo {

        private final int id;

        private final QualifiedName qN;

        private final Quoting quoting;

        private PlaceholderInfo(String s) {
            // {1:blabla:PERCENT} (or NONE or UNDERSCORE)
            String tName = s.substring(s.indexOf(":") + 1, s.indexOf("."));
            String colName = s.substring(s.indexOf(".") + 1, s.lastIndexOf(":"));
            String quoting = s.substring(s.lastIndexOf(":") + 1, s.length() - 1);
            this.id = Integer.parseInt(s.substring(1, s.indexOf(":")));
            this.qN = new QualifiedName(tName, colName);
            this.quoting = Quoting.valueOf(quoting.toUpperCase());
        }

        public int getId() {
            return this.id;
        }

        public QualifiedName getQN() {
            return this.qN;
        }

        public Quoting getQuoting() {
            return this.quoting;
        }

        public String applyQuoting(String toInsert) {
            return this.quoting.apply(toInsert);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PlaceholderInfo)) {
                return false;
            }
            PlaceholderInfo other = (PlaceholderInfo) object;
            return id == other.id && qN.equals(other.qN);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, qN);
        }

        @Override
        public String toString() {
            return "id = " + this.id + ", qN = " + this.qN;
        }

    }

}