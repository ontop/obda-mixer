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

/**
 * A fully instantiated query with template, placeholder fillers, and other metadata controlling query execution.
 */
@SuppressWarnings("unused")
public final class Query implements Serializable {

    private final @Nullable String id;

    private final @Nullable String executionId;

    private final Template template;

    private final List<String> placeholderFillers;

    private final QueryLanguage language;

    private final int timeout;

    private final boolean resultSorted;

    private final boolean resultIgnored;

    private final int attempt;

    private transient String cachedFilledTemplate;

    private transient String cachedToString;

    private Query(Builder builder) {
        this.id = builder.id;
        this.executionId = builder.executionId;
        this.template = builder.template;
        this.placeholderFillers = builder.placeholderFillers;
        this.language = builder.language;
        this.timeout = builder.timeout;
        this.resultSorted = builder.resultSorted;
        this.resultIgnored = builder.resultIgnored;
        this.attempt = builder.attempt;
    }

    public @Nullable String getId() {
        return id;
    }

    public @Nullable String getExecutionId() {
        return executionId;
    }

    public Template getTemplate() {
        return template;
    }

    public List<String> getPlaceholderFillers() {
        return placeholderFillers;
    }

    public String getFilledTemplate() {
        if (cachedFilledTemplate == null) {
            cachedFilledTemplate = template.apply(placeholderFillers);
        }
        return cachedFilledTemplate;
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

    public int getAttempt() {
        return attempt;
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
                && Objects.equals(executionId, other.executionId)
                && template.equals(other.template)
                && placeholderFillers.equals(other.placeholderFillers)
                && language == other.language
                && timeout == other.timeout
                && resultSorted == other.resultSorted
                && resultIgnored == other.resultIgnored
                && attempt == other.attempt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, executionId, template, placeholderFillers, language,
                timeout, resultSorted, resultIgnored, attempt);
    }

    public String toString(boolean verbose) {
        if (!verbose && cachedToString != null) {
            return cachedToString;
        }
        String cs = language.getCommentString();
        StringBuilder sb = new StringBuilder();
        if (executionId != null) {
            sb.append(cs).append(' ').append(executionId).append('\n');
        }
        if (attempt > 1) {
            sb.append(cs).append(" mixer:attempt=").append(attempt).append('\n');
        }
        if (verbose) {
            if (id != null) {
                sb.append(cs).append(" mixer:id=").append(id).append('\n');
            }
            sb.append(cs).append(" mixer:placeholderFillers=").append(placeholderFillers).append('\n');
            sb.append(cs).append(" mixer:language=").append(language).append('\n');
            sb.append(cs).append(" mixer:timeout=").append(timeout).append('\n');
            sb.append(cs).append(" mixer:resultSorted=").append(resultSorted).append('\n');
            sb.append(cs).append(" mixer:resultIgnored=").append(resultIgnored).append('\n');
        }
        sb.append(getFilledTemplate());
        String result = sb.toString();
        if (!verbose) {
            this.cachedToString = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder(Template template) {
        return new Builder(template);
    }

    public static final class Builder {

        private static final Pattern SORTED_PATTERN = Pattern
                .compile("(^|\\n)\\s*(--|#)\\s*mixer:sorted\\s*=\\s*true\\s*($|[\\n\\r])");

        private @Nullable String id;

        private @Nullable String executionId;

        private Template template;

        private List<String> placeholderFillers = Collections.emptyList();

        private QueryLanguage language = QueryLanguage.SPARQL;

        private int timeout;

        private boolean resultSorted;

        private boolean resultIgnored;

        private int attempt = 1;

        Builder(Template template) {
            this.template = template;
            this.resultSorted = SORTED_PATTERN.matcher(template.toString()).find();
        }

        Builder(Query query) {
            this.id = query.id;
            this.executionId = query.executionId;
            this.template = query.template;
            this.placeholderFillers = query.placeholderFillers;
            this.language = query.language;
            this.timeout = query.timeout;
            this.resultSorted = query.resultSorted;
            this.resultIgnored = query.resultIgnored;
            this.attempt = query.attempt;
        }

        public Builder withId(@Nullable String id) {
            this.id = id;
            return this;
        }

        public Builder withExecutionId(@Nullable String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder withTemplate(Template template) {
            this.template = Objects.requireNonNull(template, "Template must not be null");
            return this;
        }

        public Builder withPlaceholderFillers(@Nullable Iterable<String> placeholderFillers) {
            this.placeholderFillers = placeholderFillers == null || !placeholderFillers.iterator().hasNext()
                    ? Collections.emptyList()
                    : List.copyOf(placeholderFillers instanceof Collection<?> ? (Collection<String>) placeholderFillers
                    : StreamSupport.stream(placeholderFillers.spliterator(), false).collect(Collectors.toList()));
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

        public Builder withAttempt(@Nullable Integer attempt) {
            if (attempt == null) {
                this.attempt = 1;
            } else if (attempt >= 1) {
                this.attempt = attempt;
            } else {
                throw new IllegalArgumentException("Attempt be null or a positive number");
            }
            return this;
        }

        public Query build() {
            template.validateFillers(placeholderFillers);
            return new Query(this);
        }

    }

}
