package it.unibz.inf.mixer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class AbstractMixer extends AbstractPlugin implements Mixer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMixer.class);

    private final Set<Execution> pendingExecutions = new HashSet<>();

    @Override
    public QueryExecution prepare(Query query) {
        // Check arguments
        Objects.requireNonNull(query);

        synchronized (pendingExecutions) {
            // Fail if already closed
            if (isClosed()) {
                throw new IllegalStateException("Already closed");
            }

            // Create a new Execution object for the supplied query and add it to pending executions
            Execution execution = new Execution(query, pendingExecutions::remove);
            pendingExecutions.add(execution);
            return execution;
        }
    }

    @Override
    public void close() throws Exception {
        synchronized (pendingExecutions) {
            // Abort if already closed
            if (isClosed()) {
                return;
            }

            // Close pending Execution objects
            for (Execution execution : pendingExecutions) {
                execution.close();
            }
            pendingExecutions.clear();

            // Delegate to parent class (will mark as closed)
            super.close();
        }
    }

    protected abstract void execute(Query query, Handler handler, Context context) throws Exception;

    private final class Execution implements QueryExecution {

        // Static state enumeration constants
        private static final int STATE_CREATED = 0; // object created, neither executed or closed yet
        private static final int STATE_EXECUTING = 1; // execute() is running, close() not called
        private static final int STATE_INTERRUPTING = 2; // execute() is running and close() has been called
        private static final int STATE_COMPLETED = 3; // execute() completed or entirely skipped after calling close()

        // Member fields
        private final Query query;
        private final List<Runnable> interrupters;
        private final Consumer<Execution> closeCallback;
        private volatile int state;

        Execution(Query query, Consumer<Execution> closeCallback) {
            this.query = query;
            this.interrupters = new ArrayList<>();
            this.closeCallback = closeCallback;
            this.state = STATE_CREATED;
        }

        private Object getLock() {
            return interrupters; // reusing this object field as internal lock object not accessible by clients
        }

        boolean isInterrupted() {
            synchronized (getLock()) {
                return state == STATE_INTERRUPTING;
            }
        }

        void addInterrupter(Runnable interrupter) throws InterruptedException {
            synchronized (getLock()) {
                if (state == STATE_INTERRUPTING) {
                    // Run interrupter immediately, gathering any exception and failing with InterruptedException
                    InterruptedException ex = new InterruptedException();
                    try {
                        interrupter.run();
                    } catch (Throwable e) {
                        ex.addSuppressed(e);
                    }
                    throw ex;

                } else if (state == STATE_EXECUTING) {
                    // Add the interrupter to those to be called by close()
                    interrupters.add(interrupter);
                }
            }
        }

        @Override
        public void execute(Handler handler) throws Throwable {

            // Check argument
            Objects.requireNonNull(handler);

            // Update state ensuring that execute() is run at most once
            synchronized (getLock()) {
                if (state != STATE_CREATED) {
                    throw new IllegalStateException(state == STATE_COMPLETED ? "Already completed"
                            : "Query execution already triggered");
                }
                state = STATE_EXECUTING;
            }

            // Take start and timeout absolute timestamps
            long startTs = System.currentTimeMillis();
            long timeoutTs = query.getTimeout() <= 0 ? 0 : startTs + query.getTimeout() * 1000L;

            // Allocate the Context object to be used by execute()
            Context context = new Context(this, startTs, timeoutTs);

            // Schedule an asynchronous call to close() when the timeout occurs (if set on the query)
            if (timeoutTs > 0) {
                CompletableFuture
                        .delayedExecutor(timeoutTs - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .execute(this::close);
            }

            // Delegate to execute() passing the Context object and intercepting any exception it may throw
            Throwable throwable = null;
            try {
                AbstractMixer.this.execute(query, handler, context);
            } catch (Throwable ex) {
                throwable = ex;
            }

            // Invalidate the Context object, so that further calls to it will fail
            context.close();

            // Update state, eagerly discarding unneeded interrupt actions and determining if execution was interrupted
            boolean interrupted;
            synchronized (getLock()) {
                interrupted = state == STATE_INTERRUPTING;
                interrupters.clear();
                state = STATE_COMPLETED;
            }

            // Propagate the exception, if any, ensuring it is an InterruptedException if execution was interrupted
            if (throwable != null) {
                throw !interrupted || (throwable instanceof InterruptedException) ? throwable
                        : new InterruptedException().initCause(throwable);
            }
        }

        @Override
        public void close() {
            synchronized (getLock()) {
                if (state == STATE_CREATED) {
                    // Execution not started yet, simply switch to completed state
                    state = STATE_COMPLETED;
                    closeCallback.accept(this);

                } else if (state == STATE_EXECUTING) {
                    // Execution is in progress, switch to interrupting state and running interrupters to try halting
                    // execution (e.g., by closing open connections and the like)
                    state = STATE_INTERRUPTING;
                    for (int i = interrupters.size() - 1; i >= 0; --i) {
                        try {
                            interrupters.get(i).run();
                        } catch (Throwable e) {
                            LOGGER.error("Error while interrupting query execution (ignored)"
                                    + (e.getMessage() == null ? "" : e.getMessage()), e);
                        }
                    }
                    interrupters.clear();
                    closeCallback.accept(this);
                }
            }
        }

    }

    protected final class Context {

        /*
         * This class is implemented as a 'facade' over Execution, exposing to execute() a controlled set of
         * functionalities, without any way for implementations of execute() to get the wrapped Execution object.
         */

        private final Execution execution;
        private final long startTs;
        private final long timeoutTs;
        private volatile boolean closed;

        Context(Execution execution, long startTs, long timeoutTs) {
            this.execution = execution;
            this.startTs = startTs;
            this.timeoutTs = timeoutTs;
            this.closed = false;
        }

        void close() {
            this.closed = true;
        }

        private void checkNotClosed() {
            if (closed) {
                throw new IllegalStateException("Context can be accessed only within the scope of execute()");
            }
        }

        public long getStartTs() {
            checkNotClosed();
            return startTs;
        }

        public long getTimeoutTs() {
            checkNotClosed();
            return timeoutTs;
        }

        public boolean isInterrupted() {
            checkNotClosed();
            return execution.isInterrupted();
        }

        public void checkNotInterrupted() throws InterruptedException {
            if (isInterrupted()) {
                throw new InterruptedException();
            }
        }

        public void onInterruptRun(Runnable action) throws InterruptedException {
            Objects.requireNonNull(action);
            checkNotClosed();
            execution.addInterrupter(action);
        }

        public void onInterruptClose(AutoCloseable resource) throws InterruptedException {
            Objects.requireNonNull(resource);
            onInterruptRun(() -> {
                try {
                    resource.close();
                } catch (Throwable ex) {
                    LOGGER.error("Error while interrupting query execution (ignored)"
                            + (ex.getMessage() == null ? "" : ex.getMessage()), ex);
                }
            });
        }

    }

}
