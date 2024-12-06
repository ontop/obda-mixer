package it.unibz.inf.mixer.core;

/**
 * Enumeration of query languages.
 */
@SuppressWarnings("unused")
public enum QueryLanguage {

    SPARQL("#"),

    SQL("--");

    private final String commentString;

    QueryLanguage(String commentString) {
        this.commentString = commentString;
    }

    public String getCommentString() {
        return commentString;
    }

}
