package it.unibz.inf.mixer.core;

/**
 * An individual execution of a query through a {@link Mixer}.
 * <p>
 * An attempt to interrupt query execution within {@link #execute(Handler)}, resulting in the latter throwing an
 * {@link InterruptedException}, is expected on a best effort basis if either:
 * <ul>
 * <li>method {@link #close()} is called;</li>
 * <li>the configured {@link Query#getTimeout()} is reached, if any;</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("unused")
public interface QueryExecution extends AutoCloseable {

    /**
     * Executes the query synchronously, reporting query results and other execution events to the supplied
     * {@code Handler} object.
     *
     * @param handler handler object where to report query execution events and results
     * @throws Exception on failure, with {@link InterruptedException} being reported if query execution has been
     *                   interrupted
     */
    void execute(Handler handler) throws Throwable;

    @Override
    default void close() {
    }

}
