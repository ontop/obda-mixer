package it.unibz.inf.mixer.core;

import java.util.Map;

/**
 * Interface of an OBDA Mixer plugin.
 * <p>
 * This interface defines the basics of an OBDA Mixer plugin and is extended for specific plugin types, such as
 * {@link Mixer} and {@link QuerySelector}. A plugin consists in a class implementing this (or derived) interface and
 * its lifecycle consists of three steps:
 * <ul>
 *     <li>instantiation, performed by calling the default constructor of the plugin class;</li>
 *     <li>initialization, performed by calling method {@link #init(Map)} with user-supplied configuration properties</li>
 *     <li>execution, which depends on the specific {@code Plugin} sub-interface, {@code Query} execution for a {@code Mixer} plugin)</li>
 *     <li>disposing, performed by calling method {@link #close()} to release any resource allocated by the plugin (e.g., a JDBC connection)</li>
 * </ul>
 * Depending on thes specific plugin type ({@code Plugin} sub-interface), plugin instances may be expected to be thread safe.
 * </p>
 */
public interface Plugin extends AutoCloseable {

    /**
     * Initializes the plugin, supplying configuration data if any. This method is guaranteed to be called prior to the
     * evaluation.
     *
     * @param configuration {@code <key, value>} configuration properties, not expected to be modified
     */
    void init(Map<String, String> configuration) throws Throwable;

    /**
     * Disposes the plugin and releases any resource allocated by it, if any. This method is guaranteed to be called
     * at the end of the evaluation.
     */
    void close() throws Exception;

}
