package it.unibz.inf.mixer_shell.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.utils.persistence.LogToFile;

public class MixerShell extends Mixer {
 
    private String cmd;
    private boolean shellOutput;
    
    public MixerShell(Conf configuration) {
	super(configuration);
	this.cmd = configuration.getShellCmd();
	this.shellOutput = configuration.getShellOutput();
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

		BufferedReader in =
			new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader err =
			new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String inputLine;
		if( shellOutput ){
		    // logging
		    
		    long curTime = System.currentTimeMillis();
		    LogToFile logger = LogToFile.getInstance();
		    logger.openFile("par_" + curTime);
		    logger.appendLine(cmd);
		    logger.closeFile();
		    logger.openFile("out_" + curTime);
		    
		    while ((inputLine = in.readLine()) != null) {
			logger.appendLine(inputLine);
		    }
		    in.close();
		    while ( (inputLine = err.readLine()) != null ){
			logger.appendLine(inputLine);
		    }
		    err.close();
		    logger.closeFile();
		}else{ // noShellOutput
		    while ((inputLine = in.readLine()) != null) {}
		    in.close();
		    while ( (inputLine = err.readLine()) != null ){}
		    err.close();
		}
		p.waitFor();
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
