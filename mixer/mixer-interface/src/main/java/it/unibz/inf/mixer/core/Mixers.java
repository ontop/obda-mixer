package it.unibz.inf.mixer.core;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * Utility methods pertaining to {@link Mixer}.
 */
@SuppressWarnings("unused")
public final class Mixers {

    private static final Map<String, Map<String, String>> METADATA = loadClasspathMetadata();

    /**
     * Lists all {@link Mixer} types defined on the classpath.
     *
     * @return an immutable set of mixer types
     */
    public static Set<String> list() {
        return METADATA.keySet();
    }

    /**
     * Returns the classpath metadata for the specified {@link Mixer} type
     *
     * @param mixerType the mixer type, not null
     * @return an immutable map of {@code key, value} metadata properties describing the mixer type
     * @throws IllegalArgumentException if the mixer type is unknown
     */
    public static Map<String, String> describe(String mixerType) {
        Map<String, String> result = METADATA.get(mixerType);
        if (result == null) {
            Objects.requireNonNull(mixerType);
            throw new IllegalArgumentException("Undefined mixer type " + mixerType);
        }
        return result;
    }

    /**
     * Instantiates a {@link Mixer} of the specified mixer type.
     *
     * @param mixerType the mixer type, not null
     * @return the instantiated {@code Mixer} upon success
     * @throws IllegalArgumentException if the mixer type is unknown
     */
    public static Mixer create(String mixerType) {

        Map<String, String> metadata = describe(mixerType);

        String javaType = metadata.get("type");
        if (javaType == null) {
            throw new IllegalArgumentException("Missing 'mixers." + mixerType
                    + ".type' property with the Java class to instantiate");
        }

        Class<? extends Mixer> javaClass;
        try {
            javaClass = Class.forName(metadata.get("type")).asSubclass(Mixer.class);
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Not a valid Mixer Java class: " + javaType, ex);
        }

        try {
            Constructor<? extends Mixer> constructor = javaClass.getConstructor();
            return constructor.newInstance();
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException("Constructor call failed for Mixer Java class " + javaType + ": "
                    + ex.getMessage(), ex.getTargetException());
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Mixer Java class " + javaType
                    + " must be concrete and provide a no-arg public constructor", ex);
        }
    }

    private static Map<String, Map<String, String>> loadClasspathMetadata() {

        // Allocate result map
        Map<String, Map<String, String>> metadata = new HashMap<>();

        try {
            // Iterate and load all the classpath resources matching a specific name
            var resourceName = "META-INF/mixer.properties";
            for (Enumeration<URL> re = Mixers.class.getClassLoader().getResources(resourceName); re.hasMoreElements(); ) {
                URL url = re.nextElement();
                try (InputStream stream = url.openStream()) {
                    // Match 'mixers.xyz...' properties, associating them to mixer name 'xyz'
                    Properties props = new Properties();
                    props.load(stream);
                    for (Enumeration<?> pe = props.propertyNames(); pe.hasMoreElements(); ) {
                        String prop = (String) pe.nextElement();
                        if (prop.startsWith("mixers.")) {
                            int start = "mixers.".length();
                            int end = prop.indexOf(".", start);
                            if (end > start) {
                                String mixerType = prop.substring(start, end);
                                String mixerProp = prop.substring(end + 1);
                                String mixerValue = props.getProperty(prop);
                                metadata.computeIfAbsent(mixerType, n -> new HashMap<>()).put(mixerProp, mixerValue);
                            }
                        }
                    }
                } catch (Throwable ex) {
                    // Report error (using stderr as no logging library is used here)
                    System.err.println("Could not load mixer properties from " + url);
                    ex.printStackTrace();
                }
            }
        } catch (Throwable ex) {
            // Report error (using stderr as no logging library is used here)
            System.err.println("Could not load mixer properties");
            ex.printStackTrace();
        }

        // Freeze all the mixer properties values in the metadata map
        for (var e : metadata.entrySet()) {
            e.setValue(Collections.unmodifiableMap(e.getValue()));
        }

        // Freeze and return the metadata map
        return Collections.unmodifiableMap(metadata);
    }

    private Mixers() {
        throw new Error();
    }

}
