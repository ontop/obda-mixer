package it.unibz.inf.mixer.core;

import org.jspecify.annotations.Nullable;

/**
 * Callback interface for notifying relevant events during query execution.
 */
public interface Handler {

    void onSubmit();

    void onStartResults();

    void onSolutionIRIBinding(String variable, String iri);

    void onSolutionBNodeBinding(String variable, String id);

    void onSolutionLiteralBinding(String variable, String label, @Nullable String datatypeIri, @Nullable String lang);

    void onSolutionSQLBinding(String variable, Object value, int type);

    void onSolutionCompleted();

    void onEndResults(@Nullable Integer numSolutions);

    void onMetadata(String attribute, Object value);

}
