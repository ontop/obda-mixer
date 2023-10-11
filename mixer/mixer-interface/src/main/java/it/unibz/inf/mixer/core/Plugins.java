package it.unibz.inf.mixer.core;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods pertaining to {@link Plugin}.
 */
@SuppressWarnings("unused")
public final class Plugins {

    private static final Map<String, Map<String, String>> METADATA = loadClasspathMetadata();

    /**
     * Lists {@link Plugin} names defined on the classpath, optionally restricting to those implementing a supplied
     * list of interfaces.
     *
     * @param requiredInterfaces an optional list of implemented interfaces for matched plugins
     * @return an immutable set of plugin names
     */
    public static Set<String> list(Class<?>... requiredInterfaces) {
        if (requiredInterfaces.length == 0) {
            return METADATA.keySet();
        } else {
            return METADATA.entrySet().stream()
                    .filter(e -> {
                        try {
                            Class<?> javaClass = Class.forName(e.getValue().get("type"));
                            for (Class<?> requiredInterface : requiredInterfaces) {
                                if (!requiredInterface.isAssignableFrom(javaClass)) {
                                    return false;
                                }
                            }
                            return true;
                        } catch (Throwable ex) {
                            return false;
                        }
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Returns the classpath metadata for the specified {@link Plugin} name
     *
     * @param pluginName the plugin name, not null
     * @return an immutable map of {@code key, value} metadata properties describing the plugin
     * @throws IllegalArgumentException if the plugin name is unknown
     */
    public static Map<String, String> describe(String pluginName) {
        Map<String, String> result = METADATA.get(pluginName);
        if (result == null) {
            Objects.requireNonNull(pluginName);
            throw new IllegalArgumentException("Undefined plugin name " + pluginName);
        }
        return result;
    }

    /**
     * Instantiates a {@link Plugin} with the specified plugin name.
     *
     * @param pluginName the plugin name, not null
     * @return the instantiated {@code Plugin} upon success
     * @throws IllegalArgumentException if the plugin name is unknown
     */
    public static Plugin create(String pluginName) {

        Map<String, String> metadata = describe(pluginName);
        String javaClassName = metadata.get("type");
        Class<? extends Plugin> javaClass;
        try {
            javaClass = Class.forName(javaClassName).asSubclass(Plugin.class);
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Not a valid Plugin Java class: " + javaClassName, ex);
        }

        try {
            Constructor<? extends Plugin> constructor = javaClass.getConstructor();
            return constructor.newInstance();
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException("Constructor call failed for Plugin Java class " + javaClassName + ": "
                    + ex.getMessage(), ex.getTargetException());
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Plugin Java class " + javaClassName
                    + " must be concrete and provide a no-arg public constructor", ex);
        }
    }

    private static Map<String, Map<String, String>> loadClasspathMetadata() {

        // Allocate result map
        Map<String, Map<String, String>> metadata = new HashMap<>();

        try {
            // Iterate and load all the classpath resources matching a specific name
            var resourceName = "META-INF/mixer.properties";
            for (Enumeration<URL> re = Plugins.class.getClassLoader().getResources(resourceName); re.hasMoreElements(); ) {
                URL url = re.nextElement();
                try (InputStream stream = url.openStream()) {
                    // Match 'plugins.xyz...' properties, associating them to mixer name 'xyz'
                    Properties props = new Properties();
                    props.load(stream);
                    for (Enumeration<?> pe = props.propertyNames(); pe.hasMoreElements(); ) {
                        String prop = (String) pe.nextElement();
                        if (prop.startsWith("plugins.")) {
                            int start = "plugins.".length();
                            int end = prop.indexOf(".", start);
                            if (end > start) {
                                String pluginName = prop.substring(start, end);
                                String pluginProp = prop.substring(end + 1);
                                String pluginValue = props.getProperty(prop);
                                metadata.computeIfAbsent(pluginName, n -> new HashMap<>()).put(pluginProp, pluginValue);
                            }
                        }
                    }
                } catch (Throwable ex) {
                    // Report error (using stderr as no logging library is used here)
                    System.err.println("Could not load plugins properties from " + url);
                    ex.printStackTrace();
                }
            }
        } catch (Throwable ex) {
            // Report error (using stderr as no logging library is used here)
            System.err.println("Could not load plugins properties");
            ex.printStackTrace();
        }

        // Remove plugins lacking or with a wrong implementation class ('type' property)
        metadata.entrySet().removeIf(e -> {
            String javaClassName = e.getValue().get("type");
            if (javaClassName == null) {
                System.err.println("Ignoring plugin '" + e.getKey() + "': missing property 'plugins." + e.getKey()
                        + ".type' with the Java class to instantiate");
                return true;
            }
            try {
                Class.forName(javaClassName).asSubclass(Plugin.class);
            } catch (Throwable ex) {
                System.err.println("Ignoring plugin " + e.getKey() + ": '" + javaClassName
                        + "' is not a valid Plugin Java class");
                return true;
            }
            return false;
        });

        // Freeze all the plugins properties values in the metadata map
        for (var e : metadata.entrySet()) {
            e.setValue(Collections.unmodifiableMap(e.getValue()));
        }

        // Freeze and return the metadata map
        return Collections.unmodifiableMap(metadata);
    }

    private Plugins() {
        throw new Error();
    }

}
