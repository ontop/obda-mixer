package it.unibz.inf.mixer.core;

import java.util.Map;

/**
 * Interface of an OBDA Mixer plugin.
 * <p>
 * This interface defines the basics of an OBDA Mixer plugin and is extended for specific plugin types, that is,
 * {@link Mixer} and {@link QuerySelector}. A plugin consists in a class implementing this (or a derived) interface and
 * accompanying metadata under {@code META-INF/mixer.properties}. The lifecycle of a plugin consists of four steps:
 * <ul>
 *     <li>instantiation, performed by calling the default constructor of the plugin class;</li>
 *     <li>initialization, performed by calling method {@link #init(Map)} with user-supplied configuration properties</li>
 *     <li>execution, which depends on the specific {@code Plugin} sub-interface, {@code Query} execution for a {@code Mixer} plugin)</li>
 *     <li>disposing, performed by calling method {@link #close()} to release any resource allocated by the plugin (e.g., a JDBC connection)</li>
 * </ul>
 * Depending on thes specific plugin type ({@code Plugin} sub-interface), plugin instances may be expected to be thread safe.
 * </p>
 * <p>
 * The plugin metadata in {@code META-INF/mixer.properties} consists in a set of key-value entries where the key has
 * the format {@code plugins.PLUGIN_NAME.PROPERTY'}. These entries are scanned by utility class {@link Plugins}, based on which
 * it offers facilities for enumerating, describing, and instantiating available plugins. The following properties are
 * supported:
 * <ul>
 *     <li>{@code plugins.PLUGIN_NAME.type} (required) - fully qualified name of the Java class implementing the {@code Plugin} interface;</li>
 *     <li>{@code plugins.PLUGIN_NAME.desc} (optional) - a description of the plugin, displayed via command line help;</li>
 *     <li>{@code plugins.PLUGIN_NAME.conf.ARG_NAME.type} (required for each argument) - fully qualified name of the Java class corresponding to the parameter data type (e.g., {@code java.lang.String});</li>
 *     <li>{@code plugins.PLUGIN_NAME.conf.ARG_NAME.desc} (optional) - a description of the argument, for help purposes;</li>
 *     <li>{@code plugins.PLUGIN_NAME.conf.ARG_NAME.type.default} (optional) - the default value of the argument, for help purposes.</li>
 * </ul>
 * </p>
 *
 * @see Plugins
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
