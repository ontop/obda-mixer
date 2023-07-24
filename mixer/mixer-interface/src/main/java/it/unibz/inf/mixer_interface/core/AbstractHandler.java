package it.unibz.inf.mixer_interface.core;

import org.jspecify.annotations.Nullable;

public abstract class AbstractHandler implements Handler {

    @Override
    public void onSubmit() {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onStartResults() {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onSolutionIRIBinding(String variable, String iri) {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onSolutionBNodeBinding(String variable, String id) {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onSolutionLiteralBinding(String variable, String label, String datatypeIri, String lang) {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onSolutionSQLBinding(String variable, Object value, int type) {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onSolutionCompleted() {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onEndResults(@Nullable Integer numSolutions) {
        // Do nothing, may be overridden by subclasses
    }

    @Override
    public void onMetadata(String attribute, Object value) {
        // Do nothing, may be overridden by subclasses
    }

}
