package it.unibz.inf.mixer_main.execution;

import it.unibz.inf.mixer_db_connection.DBMSConnection;
import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_main.utils.QualifiedName;
import it.unibz.inf.mixer_main.utils.Template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TemplateQuerySelector {

    private Set<String> executedQueries = new HashSet<>();
    private String templatesDir;

    // Fields to use this class as an iterator
    private int index;
    private File[] listOfFiles;

    // Pointers in the database
    private Map<String, Integer> resultSetPointer = new HashMap<String, Integer>();

    // Connection to the database
    DBMSConnection db;

    // State
    private int nExecutedTemplate;

    // Queries to skip
    private List<String> forceTimeoutQueries;

    public TemplateQuerySelector(Conf configuration, DBMSConnection db) {
        index = 0;
        templatesDir = configuration.getTemplatesDir();

        this.db = db;

        // Query templates
        File folder = new File(templatesDir);
        listOfFiles = folder.listFiles();

        this.nExecutedTemplate = 0;

        // Force timeouts
        this.forceTimeoutQueries = configuration.getForcedTimeouts();
    }

    public String getCurQueryName() {
        return listOfFiles[index - 1].getName();
    }

    public String getNextQuery() {

        if (index >= listOfFiles.length) {
            index = 0;
            return null;
        }

        while (!listOfFiles[index].isFile()) {
            ++index;
            if (index >= listOfFiles.length) {
                index = 0;
                return null;
            }
        }
        ;

        String inFile = templatesDir + "/" + listOfFiles[index].getName();
        String result = null;
        String queryName = listOfFiles[index].getName();

        if (this.forceTimeoutQueries.contains(queryName)) {
            // Forcibly timeout the query
            ++index;
            return "force-timeout";
        }

        try {
            BufferedReader in;
            in = new BufferedReader(new FileReader(inFile));

            StringBuilder queryBuilder = new StringBuilder();
            String curLine = null;

            while ((curLine = in.readLine()) != null) {
                queryBuilder.append(curLine + "\n");
            }
            in.close();

            Template sparqlQueryTemplate = new Template(queryBuilder.toString());
            int maxTries = 200;
            int i = 0;
            do {
                fillPlaceholders(sparqlQueryTemplate);
            }
            while (i++ < maxTries && sparqlQueryTemplate.getNumPlaceholders() != 0 && executedQueries.contains(sparqlQueryTemplate.getFilled()));

            result = sparqlQueryTemplate.getFilled();
            executedQueries.add(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ++index;
        return result;
    }

    private void fillPlaceholders(Template sparqlQueryTemplate) {

        MixerThread.log.debug("[mixer-debug] Call fillPlaceholders");

        Map<Template.PlaceholderInfo, String> mapTIToValue = new HashMap<Template.PlaceholderInfo, String>();

        this.resultSetPointer.clear();
        ++this.nExecutedTemplate;

        if (sparqlQueryTemplate.getNumPlaceholders() == 0) return;

        for (int i = 1; i <= sparqlQueryTemplate.getNumPlaceholders(); ++i) {

            Template.PlaceholderInfo info = sparqlQueryTemplate.getNthPlaceholderInfo(i);

            String toInsert = null;
            if (mapTIToValue.containsKey(info)) {
                toInsert = mapTIToValue.get(info);
            } else {
                toInsert = findValueToInsert(info.getQN());
                mapTIToValue.put(info, toInsert);
            }
            toInsert = info.applyQuote(toInsert, info.quote());
            sparqlQueryTemplate.setNthPlaceholder(i, toInsert);
        }
    }

    /**
     * Tries to look in the same row, as long as possible
     *
     * @param qN
     * @return
     */
    private String findValueToInsert(QualifiedName qN) {

        String result = null;
        int pointer = 0;

        if (resultSetPointer.containsKey(qN.toString())) {
            pointer = resultSetPointer.get(qN.toString());
            resultSetPointer.put(qN.toString(), pointer + 1);
        } else {
            resultSetPointer.put(qN.toString(), 1);
        }

        pointer += this.nExecutedTemplate;
        String query = "SELECT " + "*" + " FROM "
                + qN.getFirst() + " WHERE " + qN.getSecond() + " IS NOT NULL "
                + " LIMIT " + pointer + ", 1";

        if (db.getJdbcConnector().equals("jdbc:postgresql")) {
            query = "SELECT " +
                    "*" +
                    // qN.getSecond()+
                    " FROM \"" + qN.getFirst() + "\" WHERE \""
                    + qN.getSecond() + "\" IS NOT NULL LIMIT 1" +
                    " OFFSET " + pointer+ ";";
        }

        Connection conn = db.getConnection();
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(query);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        try {
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                stmt.close();
                query = "SELECT  " + "*" + " FROM "
                        + qN.getFirst() + " LIMIT " + 0 + ", 1";
                resultSetPointer.put(qN.toString(), 1);

                if (db.getJdbcConnector().equals("jdbc:postgresql")) {
                    query = "SELECT  \"" +
                            qN.getSecond() +
                            "\" FROM \"" + qN.getFirst() + "\" WHERE \""
                            + qN.getSecond() + "\" IS NOT NULL LIMIT 1" +
                            " OFFSET " + 0 + ";";
                }

                stmt = conn.prepareStatement(query);

                rs = stmt.executeQuery();
                if (!rs.next()) {
                    String msg = "Unexpected Problem: No result to fill placeholder. Contact the developers.";
                    MixerMain.closeEverything(msg);
                }
            }
            result = rs.getString(qN.getSecond());
        } catch (SQLException e) {
            try {
                conn.close();
            } catch (SQLException e1) {
                String msg = "Could not close Connection.";
                MixerMain.closeEverything(msg, e1);
            }
            MixerMain.closeEverything("Error while executing the SQL statement.", e);
        }
        return result;
    }
};
