package it.unibz.inf.mixer.shell;

import it.unibz.inf.mixer.core.AbstractPlugin;
import it.unibz.inf.mixer.core.Handler;
import it.unibz.inf.mixer.core.Mixer;
import it.unibz.inf.mixer.core.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class MixerShell extends AbstractPlugin implements Mixer {

    private String cmd;
    private boolean shellOutput;

    @Override
    public void init(Map<String, String> conf) throws Exception {
        super.init(conf);
        this.cmd = getConfiguration().get("shell-cmd");
        this.shellOutput = Boolean.parseBoolean(getConfiguration().getOrDefault("shell-out", "false"));
    }

    @Override
    public void execute(Query query, Handler handler) throws Exception {
        handler.onSubmit();
        Object results = executeQuery(query.toString(), query.getTimeout());
        handler.onStartResults();
        int numSolutions = traverseResultSet(results);
        handler.onEndResults(numSolutions);
    }

    private class ExecuterThread extends Thread {

        private String cmd;
        private Process p;

        ExecuterThread(String cmd) {
            this.cmd = cmd;
        }

        public void run() {
            execute(this.cmd);
        }

        long execute(String cmd) {
            long startTime = System.currentTimeMillis();
            try {
                p = Runtime.getRuntime().exec(cmd);

                BufferedReader in =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader err =
                        new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String inputLine;
                if (shellOutput) {
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
                    while ((inputLine = err.readLine()) != null) {
                        logger.appendLine(inputLine);
                    }
                    err.close();
                    logger.closeFile();
                } else { // noShellOutput
                    while ((inputLine = in.readLine()) != null) {
                    }
                    in.close();
                    while ((inputLine = err.readLine()) != null) {
                    }
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

    public Object executeQuery(String query, int timeout) {
        // Timeout to be handled at script-side
        String cmd = this.cmd + " '" + query.replaceAll("\n", " ") + "'";
        ExecuterThread executer = new ExecuterThread(cmd);
        executer.start();
        try {
            executer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new NormalShellResult();
    }

    public int traverseResultSet(Object resultSet) {
        // Unsupported
        return 0;
    }

};

abstract class ShellResult {
};

class NormalShellResult extends ShellResult {
}
