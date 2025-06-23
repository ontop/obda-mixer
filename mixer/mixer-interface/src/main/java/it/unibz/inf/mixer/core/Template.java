package it.unibz.inf.mixer.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * A generic string template providing placeholders and instantiable with concrete placeholder fillers.
 * <p>
 * Placeholders follow syntax {@code ${name:quoting}}, or simply {@code ${name}} that assumes {@code none} as quoting:
 * <ul>
 *     <li>{@code name} refers to a variable supplied at template instantiation time;</li>
 *     <li>{@code quoting} optionally specify how to quote the variable value at instantiation time, by either replacing
 *     spaces with underscores or {@code %20} sequences.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("unused")
public final class Template {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[$][{]([^}]+)[}]");

    private final List<Placeholder> placeholders;

    private final String string;

    public Template(String templateString) {
        this.placeholders = Collections.unmodifiableList(Placeholder.detect(templateString));
        this.string = templateString;
    }

    public List<Placeholder> getPlaceholders() {
        return placeholders;
    }

    public void validateFillers(Iterable<String> fillers) {
        if (fillers instanceof Collection<?>) {
            doValidateFillers(((Collection<?>) fillers).size());
        } else {
            doValidateFillers((int) StreamSupport.stream(fillers.spliterator(), false).count());
        }
    }

    public void validateFillers(String... fillers) {
        doValidateFillers(fillers.length);
    }

    private void doValidateFillers(int numFillers) {
        if (numFillers != placeholders.size()) {
            throw new IllegalArgumentException("Expected " + placeholders.size() + " placeholder fillers, "
                    + numFillers + " supplied");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T extends Appendable> T apply(T out, Iterable<String> placeholderFillers) throws IOException {
        validateFillers(placeholderFillers);
        //noinspection unchecked
        return placeholders.isEmpty() ? (T) out.append(string) : doApply(out, placeholderFillers);
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T extends Appendable> T apply(T out, String... placeholderFillers) throws IOException {
        validateFillers(placeholderFillers);
        //noinspection unchecked
        return placeholders.isEmpty() ? (T) out.append(string) : doApply(out, Arrays.asList(placeholderFillers));
    }

    public String apply(Iterable<String> placeholderFillers) {
        validateFillers(placeholderFillers);
        if (placeholders.isEmpty()) {
            return string;
        } else {
            try {
                return doApply(new StringBuilder(), placeholderFillers).toString();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public String apply(String... placeholderFillers) {
        validateFillers(placeholderFillers);
        return placeholders.isEmpty() ? string : apply(Arrays.asList(placeholderFillers));
    }

    private <T extends Appendable> T doApply(T out, Iterable<String> placeholderFillers) throws IOException {
        int offset = 0, i = 0;
        for (String filler : placeholderFillers) {
            Placeholder p = placeholders.get(i++);
            if (p.getStart() > offset) {
                out.append(string.substring(offset, p.start));
            }
            if (filler != null) {
                out.append(p.getQuoting().apply(filler));
            }
            offset = p.getEnd();
        }
        if (offset < string.length()) {
            out.append(string.substring(offset));
        }
        return out;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Template)) {
            return false;
        }
        Template other = (Template) object;
        return string.equals(other.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    public String toString() {
        return string;
    }

    public enum Quoting {

        NONE,

        UNDERSCORE,

        PERCENT;

        public String apply(String toInsert) {
            switch (this) {
                case NONE:
                    return toInsert;
                case PERCENT:
                    return toInsert.replaceAll(" ", "%20");
                case UNDERSCORE:
                    return toInsert.replaceAll(" ", "_");
                default:
                    throw new IllegalArgumentException("Unsupported quoting " + this);
            }
        }

    }

    @SuppressWarnings("unused")
    public static final class Placeholder {

        private static final Pattern PATTERN = Pattern.compile("[$][{]([^}]+)[}]");

        private final int index;

        private final int start;

        private final int end;

        private final String name;

        private final Quoting quoting;

        private Placeholder(int index, int start, int end, String s) {
            int idx = s.lastIndexOf(":");
            this.index = index;
            this.start = start;
            this.end = end;
            this.name = idx < 0 ? s.trim() : s.substring(0, idx).trim();
            this.quoting = idx < 0 ? Quoting.NONE : Quoting.valueOf(s.substring(idx + 1).trim().toUpperCase());
        }

        public int getIndex() {
            return index;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getName() {
            return name;
        }

        public Quoting getQuoting() {
            return this.quoting;
        }

        @Override
        public String toString() {
            return name;
        }

        public static List<Placeholder> detect(String templateString) {
            Objects.requireNonNull(templateString);
            List<Placeholder> result = new ArrayList<>();
            Matcher matcher = PATTERN.matcher(templateString);
            int index = 0;
            while (matcher.find()) {
                result.add(new Placeholder(index++, matcher.start(), matcher.end(), matcher.group(1)));
            }
            return result;
        }

    }

}