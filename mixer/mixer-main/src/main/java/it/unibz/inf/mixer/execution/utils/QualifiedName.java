package it.unibz.inf.mixer.execution.utils;

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

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public final class QualifiedName implements Serializable, Comparable<QualifiedName> {

    private final String first;

    private final String second;

    public QualifiedName(String tableName, String colName) {
        this.first = Objects.requireNonNull(tableName);
        this.second = Objects.requireNonNull(colName);
    }

    public QualifiedName(String csvName) {
        String[] splits = csvName.split("\\s+|\\.");
        this.first = splits[0];
        this.second = splits[1];
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    @Override
    public int compareTo(QualifiedName other) {
        int result = first.compareTo(other.first);
        if (result == 0) {
            result = second.compareTo(other.second);
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof QualifiedName)) {
            return false;
        }
        QualifiedName other = (QualifiedName) object;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public String toString() {
        return first + "." + second;
    }

}
