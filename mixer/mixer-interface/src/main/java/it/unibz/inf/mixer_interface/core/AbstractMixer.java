package it.unibz.inf.mixer_interface.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractMixer implements Mixer {

    private Map<String, String> configuration;

    public final Map<String, String> getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("Mixer not initialized");
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
        // Do nothing, may be overridden by subclasses
    }

}
