package it.unibz.inf.mixer.core;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public final class Query implements Serializable {

    private final @Nullable String id;

    private final List<String> placeholders;

    private final @Nullable String executionId;

    private final String string;

    private final QueryLanguage language;

    private final int timeout;

    private final boolean resultSorted;

    private final boolean resultIgnored;

    private Query(Builder builder) {
        this.id = builder.id;
        this.placeholders = builder.placeholders;
        this.executionId = builder.executionId;
        this.string = builder.string;
        this.language = builder.language;
        this.timeout = builder.timeout;
        this.resultSorted = builder.resultSorted;
        this.resultIgnored = builder.resultIgnored;
    }

    public @Nullable String getId() {
        return id;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }

    public @Nullable String getExecutionId() {
        return executionId;
    }

    public String getString() {
        return string;
    }

    public QueryLanguage getLanguage() {
        return language;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isResultSorted() {
        return resultSorted;
    }

    public boolean isResultIgnored() {
        return resultIgnored;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Query)) {
            return false;
        }
        Query other = (Query) object;
        return Objects.equals(id, other.id)
                && placeholders.equals(other.placeholders)
                && Objects.equals(executionId, other.executionId)
                && string.equals(other.string)
                && language == other.language
                && timeout == other.timeout
                && resultSorted == other.resultSorted
                && resultIgnored == other.resultIgnored;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, placeholders, executionId, string, language, timeout, resultSorted, resultIgnored);
    }

    public String toString(boolean verbose) {
        if (!verbose) {
            return string;
        } else {
            String cs = language.getCommentString();
            return cs + " mixer:id=" + id +
                    cs + " mixer:placeholders=" + placeholders +
                    cs + " mixer:executionId=" + placeholders +
                    cs + " mixer:language=" + language + "\n" +
                    cs + " mixer:timeout=" + timeout + "\n" +
                    cs + " mixer:resultSorted=" + resultSorted + "\n" +
                    cs + " mixer:resultIgnored=" + resultIgnored + "\n" +
                    string;
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder(String string) {
        return new Builder(string);
    }

    public static final class Builder {

        private static final Pattern SORTED_PATTERN = Pattern
                .compile("(^|\\n)\\s*(--|#)\\s*mixer:sorted\\s*=\\s*true\\s*($|[\\n\\r])");

        private @Nullable String id;

        private List<String> placeholders = Collections.emptyList();

        private @Nullable String executionId;

        private String string;

        private QueryLanguage language = QueryLanguage.SPARQL;

        private int timeout;

        private boolean resultSorted;

        private boolean resultIgnored;

        Builder(String string) {
            this.string = string;
            this.resultSorted = SORTED_PATTERN.matcher(string).find();
        }

        Builder(Query query) {
            this.id = query.id;
            this.placeholders = query.placeholders;
            this.string = query.string;
            this.language = query.language;
            this.timeout = query.timeout;
            this.resultSorted = query.resultSorted;
            this.resultIgnored = query.resultIgnored;
        }

        public Builder withId(@Nullable String id) {
            this.id = id;
            return this;
        }

        public Builder withPlaceholders(@Nullable Iterable<String> placeholders) {
            this.placeholders = placeholders == null || !placeholders.iterator().hasNext()
                    ? Collections.emptyList()
                    : List.copyOf(placeholders instanceof Collection<?> ? (Collection<String>) placeholders
                    : StreamSupport.stream(placeholders.spliterator(), false).collect(Collectors.toList()));
            return this;
        }

        public Builder withExecutionId(@Nullable String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder withString(String string) {
            this.string = Objects.requireNonNull(string, "Query string must not be null");
            return this;
        }

        public Builder withLanguage(@Nullable QueryLanguage language) {
            this.language = language != null ? language : QueryLanguage.SPARQL;
            return this;
        }

        public Builder withTimeout(@Nullable Integer timeout) {
            if (timeout == null) {
                this.timeout = 0;
            } else if (timeout >= 0) {
                this.timeout = timeout;
            } else {
                throw new IllegalArgumentException("Timeout must be null or a non-negative number of seconds");
            }
            return this;
        }

        public Builder withResultSorted(@Nullable Boolean resultSorted) {
            this.resultSorted = resultSorted != null ? resultSorted : false;
            return this;
        }

        public Builder withResultIgnored(@Nullable Boolean resultIgnored) {
            this.resultIgnored = resultIgnored != null ? resultIgnored : false;
            return this;
        }

        public Query build() {
            return new Query(this);
        }

    }

}
