package it.unibz.inf.mixer.core;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility methods pertaining to {@link Handler}.
 */
@SuppressWarnings("unused")
public final class Handlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Handlers.class);

    public static Handler nil() {
        return NilHandler.INSTANCE;
    }

    public static Handler validator() {
        return ValidatingHandler.INSTANCE;
    }

    public static Handler logger() {
        return logger(null);
    }

    public static Handler logger(@Nullable Consumer<String> sink) {
        return sink == null ? LoggingHandler.DEFAULT : new LoggingHandler(sink);
    }

    public static Handler compose(Handler... handlers) {

        // Check supplied handlers are not null, discard NIL and unpack any composite handler
        List<Handler> delegates = new ArrayList<>();
        for (Handler handler : handlers) {
            Objects.requireNonNull(handler);
            if (handler instanceof CompositeHandler) {
                delegates.addAll(Arrays.asList(((CompositeHandler) handler).delegates));
            } else if (handler != nil()) {
                delegates.add(handler);
            }
        }

        // Handle three cases
        if (delegates.isEmpty()) {
            return nil();
        } else if (delegates.size() == 1) {
            return Objects.requireNonNull(handlers[0]);
        } else {
            return new CompositeHandler(delegates.toArray(Handler[]::new));
        }
    }

    private Handlers() {
        throw new Error();
    }

    // HANDLER IMPLEMENTATIONS

    private static final class NilHandler extends AbstractHandler {

        public static NilHandler INSTANCE = new NilHandler();

    }

    private static final class ValidatingHandler implements Handler {

        public static ValidatingHandler INSTANCE = new ValidatingHandler();

        private static final int STATE_NEW = 0;

        private static final int STATE_SUBMITTED = 1;

        private static final int STATE_RESULTS_STARTED = 2;

        private static final int STATE_RESULTS_ENDED = 3;

        private int state = STATE_NEW;

        @Override
        public void onSubmit() {
            checkState(STATE_NEW);
            state = STATE_SUBMITTED;
        }

        @Override
        public void onStartResults() {
            checkState(STATE_SUBMITTED);
            state = STATE_RESULTS_STARTED;
        }

        @Override
        public void onSolutionIRIBinding(String variable, String iri) {
            Objects.requireNonNull(variable);
            Objects.requireNonNull(iri);
            checkState(STATE_RESULTS_STARTED);
        }

        @Override
        public void onSolutionBNodeBinding(String variable, String id) {
            Objects.requireNonNull(variable);
            Objects.requireNonNull(id);
            checkState(STATE_RESULTS_STARTED);
        }

        @Override
        public void onSolutionLiteralBinding(String variable, String label, String datatypeIri, @Nullable String lang) {
            Objects.requireNonNull(variable);
            Objects.requireNonNull(label);
            Objects.requireNonNull(datatypeIri);
            checkState(STATE_RESULTS_STARTED);
        }

        @Override
        public void onSolutionSQLBinding(String variable, Object value, int type) {
            Objects.requireNonNull(variable);
            Objects.requireNonNull(value);
            checkState(STATE_RESULTS_STARTED);
        }

        @Override
        public void onSolutionCompleted() {
            checkState(STATE_RESULTS_STARTED);
        }

        @Override
        public void onEndResults(@Nullable Integer numSolutions) {
            if (numSolutions != null && numSolutions < 0) {
                throw new IllegalArgumentException("Invalid number of solutions " + numSolutions
                        + ", must be null or non-negative");
            }
            checkState(STATE_RESULTS_STARTED);
            state = STATE_RESULTS_ENDED;
        }

        @Override
        public void onMetadata(String attribute, Object value) {
            Objects.requireNonNull(attribute);
            Objects.requireNonNull(value);
        }

        private void checkState(int expectedState) {
            if (state != expectedState) {
                throw new IllegalStateException("Callback not acceptable (expected state: "
                        + describeState(state) + "; current state: " + describeState(expectedState));
            }
        }

        private static String describeState(int state) {
            switch (state) {
                case STATE_NEW:
                    return "query not submitted";
                case STATE_SUBMITTED:
                    return "query submitted";
                case STATE_RESULTS_STARTED:
                    return "processing query results";
                case STATE_RESULTS_ENDED:
                    return "processing completed";
                default:
                    throw new Error();
            }
        }

    }

    private static final class LoggingHandler implements Handler {

        public static final LoggingHandler DEFAULT = new LoggingHandler(LOGGER::debug);

        private final Consumer<String> sink;

        public LoggingHandler(Consumer<String> sink) {
            this.sink = Objects.requireNonNull(sink);
        }

        @Override
        public void onSubmit() {
            sink.accept("query submitted");
        }

        @Override
        public void onStartResults() {
            sink.accept("query results obtained");
        }

        @Override
        public void onSolutionIRIBinding(String variable, String iri) {
            sink.accept("binding: " + variable + " = <" + iri + ">");
        }

        @Override
        public void onSolutionBNodeBinding(String variable, String id) {
            sink.accept("binding: " + variable + " = _:" + id);
        }

        @Override
        public void onSolutionLiteralBinding(String variable, String label, String datatypeIri, @Nullable String lang) {
            if (lang != null) {
                sink.accept("binding: " + variable + " = \"" + label + "\"@" + lang);
            } else {
                sink.accept("binding: " + variable + " = \"" + label + "^^<" + datatypeIri + ">");
            }
        }

        @Override
        public void onSolutionSQLBinding(String variable, Object value, int type) {
            sink.accept("binding: " + variable + " = \"" + value + "\" (SQL " + type + ")");
        }

        @Override
        public void onSolutionCompleted() {
            sink.accept("solution completed");
        }

        @Override
        public void onEndResults(@Nullable Integer numSolutions) {
            sink.accept("query results processed");
        }

        @Override
        public void onMetadata(String attribute, Object value) {
            sink.accept("metadata: " + attribute + " = " + value);
        }

    }

    private static final class CompositeHandler implements Handler {

        private final Handler[] delegates;

        public CompositeHandler(Handler[] delegates) {
            this.delegates = delegates;
        }

        @Override
        public void onSubmit() {
            for (Handler delegate : delegates) {
                delegate.onSubmit();
            }
        }

        @Override
        public void onStartResults() {
            for (Handler delegate : delegates) {
                delegate.onStartResults();
            }
        }

        @Override
        public void onSolutionIRIBinding(String variable, String iri) {
            for (Handler delegate : delegates) {
                delegate.onSolutionIRIBinding(variable, iri);
            }
        }

        @Override
        public void onSolutionBNodeBinding(String variable, String id) {
            for (Handler delegate : delegates) {
                delegate.onSolutionBNodeBinding(variable, id);
            }
        }

        @Override
        public void onSolutionLiteralBinding(String variable, String label, String datatypeIri, String lang) {
            for (Handler delegate : delegates) {
                delegate.onSolutionLiteralBinding(variable, label, datatypeIri, lang);
            }
        }

        @Override
        public void onSolutionSQLBinding(String variable, Object value, int type) {
            for (Handler delegate : delegates) {
                delegate.onSolutionSQLBinding(variable, value, type);
            }
        }

        @Override
        public void onSolutionCompleted() {
            for (Handler delegate : delegates) {
                delegate.onSolutionCompleted();
            }
        }

        @Override
        public void onEndResults(@Nullable Integer numSolutions) {
            for (Handler delegate : delegates) {
                delegate.onEndResults(numSolutions);
            }
        }

        @Override
        public void onMetadata(String attribute, Object value) {
            for (Handler delegate : delegates) {
                delegate.onMetadata(attribute, value);
            }
        }

    }

}
