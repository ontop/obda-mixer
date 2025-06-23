package it.unibz.inf.mixer.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract implementation of {@code Plugin} providing basic initialization, configuration and closing logic.
 * <p>
 * Please extend the more specific {@link AbstractMixer} when defining a custom {@link Mixer} class.
 * </p>
 */
public abstract class AbstractPlugin implements Plugin {

    private boolean closed = false;

    private Map<String, String> configuration;

    public boolean isClosed() {
        return closed;
    }

    public final Map<String, String> getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("Plugin not initialized");
        }
        return configuration;
    }

    @Override
    public void init(Map<String, String> configuration) throws Exception {
        // Save the configuration, may be overridden by subclasses
        this.configuration = Collections.unmodifiableMap(new LinkedHashMap<>(configuration));
    }

    @Override
    public void close() throws Exception {
        closed = true;
        // Do nothing, may be overridden by subclasses
    }

}
