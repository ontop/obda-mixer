package it.unibz.inf.mixer.web;

import it.unibz.inf.mixer.core.AbstractMixer;
import it.unibz.inf.mixer.core.Handler;
import it.unibz.inf.mixer.core.Query;
import it.unibz.inf.mixer.core.QueryLanguage;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public final class MixerWeb extends AbstractMixer {

    private String serviceUrl;

    @Override
    public void init(Map<String, String> configuration) throws Exception {
        super.init(configuration);
        this.serviceUrl = Objects.requireNonNull(getConfiguration().get("url"), "No service URL supplied");
    }

    @Override
    public void execute(Query query, Handler handler) throws Exception {

        // Check arguments
        Objects.requireNonNull(query);
        Objects.requireNonNull(handler);
        if (query.getLanguage() != QueryLanguage.SPARQL) {
            throw new IllegalArgumentException("Unsupported query language " + query.getLanguage() + " (expected SPARQL)");
        }

        // Extract relevant query parameters
        String queryString = query.getString();
        int timeout = query.getTimeout() * 1000;

        // Create a query object encapsulating HTTP logic and providing for proper resource management
        try (QueryExecution qe = new QueryExecution(serviceUrl, queryString, timeout)) {

            // Execute the query and wait for response stream (null on timeout)
            handler.onSubmit();
            InputStream resultStream = qe.submit();
            if (resultStream == null) {
                return; // timed out
            }
            handler.onStartResults(); // not timed out

            // Skip processing of results (reporting unknown # of solutions), if they can be ignored
            if (query.isResultIgnored()) {
                handler.onEndResults(null);
                return;
            }

            // Otherwise process results using SAX and a SAX handler reporting to the received Handler object
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(false);
            SAXParser saxParser = saxFactory.newSAXParser();
            QueryResultsHandler saxHandler = new QueryResultsHandler(handler);
            saxParser.parse(resultStream, saxHandler);
        }
    }

    public static final class QueryExecution implements Closeable {

        private final HttpURLConnection conn;

        private boolean connected = false;

        public QueryExecution(String serviceURL, String query, int timeout) throws IOException {

            // Assemble the URL for the HTTP GET operation
            char delimiter = serviceURL.indexOf('?') == -1 ? '?' : '&';
            String urlString = serviceURL + delimiter + "query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            URL url = new URL(urlString);

            // Obtain an HttpURLConnection object for the URL
            conn = (HttpURLConnection) url.openConnection();

            // Configure the HttpURLConnection object
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/sparql-results+xml");
            conn.setDefaultUseCaches(false);
            conn.setUseCaches(false);
            conn.setDoOutput(false);
            if (timeout != 0) {
                conn.setReadTimeout(timeout);
            }
        }

        public InputStream submit() throws IOException {
            try {
                // Issue the HTTP request and mark as 'connected' so to ensure 'disconnect' will be called on close
                conn.connect();
                connected = true;

                // Check server response code
                int rc = conn.getResponseCode();
                if (rc < 200 || rc >= 300) {
                    throw new IOException("Received error code " + rc + " from server with message: " + conn.getResponseMessage());
                }

                // Obtain and return response body stream (this operation may fail)
                return conn.getInputStream();

            } catch (SocketTimeoutException e) {
                // On read timeout (raised by connect()) return no stream
                return null;
            }
        }

        public void close() throws IOException {

            // Do nothing if not connected
            if (!connected) {
                return;
            }

            try {
                try {
                    // Try to consume the response stream, and in any case close it
                    try (InputStream stream = conn.getInputStream()) {
                        try {
                            while (stream.read() != -1) {
                                //noinspection ResultOfMethodCallIgnored
                                stream.skip(1024 * 1024L);
                            }
                        } catch (Throwable ex) {
                            // ignore
                        }
                    }
                } finally {
                    // In any case, disconnect the HttpURLConnection object
                    conn.disconnect();
                }
            } finally {
                // Mark as disconnected
                connected = false;
            }
        }

    }

    private static final class QueryResultsHandler extends DefaultHandler {

        private final Handler handler;

        private final StringBuilder currentText;

        private boolean processText;

        private String currentVariable;

        private String currentDatatype;

        private String currentLang;

        private int numSolutions;

        public QueryResultsHandler(Handler handler) {
            this.handler = handler;
            this.currentText = new StringBuilder();
            this.currentText.ensureCapacity(1024);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qname, Attributes attrs) {
            switch (qname) {
                case "binding":
                    currentVariable = attrs.getValue("name");
                    break;
                case "uri":
                case "bnode":
                    processText = true;
                    break;
                case "literal":
                    processText = true;
                    currentDatatype = attrs.getValue("datatype");
                    currentLang = attrs.getValue("xml:lang");
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (processText) {
                currentText.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qname) {
            switch (qname) {
                case "uri":
                    handler.onSolutionIRIBinding(currentVariable, currentText.toString());
                    currentText.setLength(0);
                    processText = false;
                    break;
                case "bnode":
                    handler.onSolutionBNodeBinding(currentVariable, currentText.toString());
                    currentText.setLength(0);
                    processText = false;
                    break;
                case "literal":
                    handler.onSolutionLiteralBinding(currentVariable, currentText.toString(), currentDatatype, currentLang);
                    currentText.setLength(0);
                    processText = false;
                    break;
                case "result":
                    handler.onSolutionCompleted();
                    ++numSolutions;
                    break;
                case "results":
                    handler.onEndResults(numSolutions);
                    break;
            }
        }

    }

}
