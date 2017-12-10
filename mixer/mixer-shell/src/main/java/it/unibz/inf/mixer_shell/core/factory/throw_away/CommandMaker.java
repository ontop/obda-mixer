package it.unibz.inf.mixer_shell.core.factory.throw_away;

public interface CommandMaker {
    
    /**
     * 
     * @return The shell string to be executed
     */
    public String make(String execPath, String query, String mapping);
}
