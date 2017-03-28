package it.unibz.inf.mixer_shell.core;
import java.io.IOException;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;

public class MixerShell extends Mixer {
 
    private String cmd;
    
    public MixerShell(Conf configuration) {
	super(configuration);
	this.cmd = configuration.getShellCmd();
    }
    
    private class ExecuterThread extends Thread{
	
	private String cmd;
	private Process p;

	ExecuterThread(String cmd){
	    this.cmd = cmd;
	}
	
	public void run() {
	    execute(this.cmd);
	}
	
	long execute(String cmd){
	    long startTime = System.currentTimeMillis();
	    try {
		p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
//		p.getInputStream();
	    } catch (IOException | InterruptedException e) {
		e.printStackTrace();
	    }
	    long endTime = System.currentTimeMillis();
	    return endTime - startTime;
	}
    }
    
    @Override
    public void load() {
	// Unsupported
    }

    @Override
    public void executeWarmUpQuery(String query) {
	String cmd = this.cmd + " '"+ query.replaceAll("\n", " ") +"'";
	ExecuterThread executer = new ExecuterThread(cmd);
	executer.start();
	try {
	    executer.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void executeWarmUpQuery(String query, int timeout) {
	// Timeout to be handled at script-side
	executeWarmUpQuery(query);
    }
    
    @Override
    public Object executeQuery(String query) {
	String cmd = this.cmd + " '"+ query.replaceAll("\n", " ") +"'";
	ExecuterThread executer = new ExecuterThread(cmd);
	executer.start();
	try {
	    executer.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	return new NormalShellResult(); 
    }

    @Override
    public Object executeQuery(String query, int timeout) {
	// Timeout to be handled at script-side
	return executeQuery(query);
    }

    @Override
    public int traverseResultSet(Object resultSet) {
	// Unsupported
	return 0;
    }

    @Override
    public long getRewritingTime() {
	// Unsupported
	return 0;
    }

    @Override
    public long getUnfoldingTime() {
	// Unsupported
	return 0;
    }

    @Override
    public String getUnfolding() {
	// Unsupported
	return null;
    }

    @Override
    public int getUnfoldingSize() {
	// Unsupported
	return 0;
    }

    @Override
    public String getRewriting() {
	// Unsupported
	return null;
    }

    @Override
    public int getRewritingSize() {
	// Unsupported
	return 0;
    }

    @Override
    public void rewritingOFF() {
	// Unsupported
    }

    @Override
    public void rewritingON() {
	// Unsupported
    }
};

abstract class ShellResult{ };

class NormalShellResult extends ShellResult{
}
