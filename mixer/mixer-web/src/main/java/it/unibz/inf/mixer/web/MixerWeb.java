package it.unibz.inf.mixer.web;

import it.unibz.inf.mixer.core.*;
import org.jspecify.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MixerWeb extends AbstractMixer {

    private String serviceUrl;
    private @Nullable Path responsePath;

    @Override
    public void init(Map<String, String> configuration) throws Exception {

        // Delegate, storing configuration
        super.init(configuration);

        // Extract relevant configuration options
        this.serviceUrl = Objects.requireNonNull(getConfiguration().get("url"), "No service URL supplied");
        this.responsePath = Optional.ofNullable(getConfiguration().get("response-path")).map(Paths::get).orElse(null);
    }

    @Override
    public QueryExecution prepare(Query query) {

        // Validate query language
        if (query.getLanguage() != QueryLanguage.SPARQL) {
            throw new IllegalArgumentException("Unsupported query language " + query.getLanguage() + " (expected SPARQL)");
        }

        // Delegate
        return super.prepare(query);
    }

    @Override
    protected void execute(Query query, Handler handler, Context context) throws Exception {

        // Prepare the HttpURLConnection to use for submitting the SPARQL query
        try (SparqlHttpRequest request = new SparqlHttpRequest(serviceUrl, query)) {

            // Register closing the request object as a way to quickly interrupt query execution
            context.onInterruptClose(request);

            // Notify the handler prior to submitting the request
            handler.onSubmit();

            // Submit the request and obtain the response stream (may fail or timeout)
            InputStream responseStream = request.submit(context.getTimeoutTs());

            // Notify handler of results availability
            handler.onStartResults();

            // Skip processing of results (reporting unknown # of solutions), if they can be ignored
            if (query.isResultIgnored()) {
                handler.onEndResults(null);
                return;
            }

            // Save the raw response body, if configured
            if (responsePath != null && query.getExecutionId() != null) {
                String filename = query.getExecutionId()
                        .replaceAll("[^a-zA-Z0-9]+", "_")
                        .replaceAll("_+", "_")
                        .replaceAll("^_|_$", "");
                Path responseFile = responsePath.resolve(filename + ".xml");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                responseStream.transferTo(bos);
                byte[] responseBytes = bos.toByteArray();
                Files.write(responseFile, responseBytes);
                responseStream = new ByteArrayInputStream(responseBytes); // replay from buffer
            }

            // Process response budy using SAX and a SAX handler reporting to the supplied Handler object
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(false);
            SAXParser saxParser = saxFactory.newSAXParser();
            saxParser.parse(responseStream, new SparqlHttpResponseHandler(handler));
        }
    }

    private static class SparqlHttpRequest implements AutoCloseable {

        private final HttpURLConnection conn;
        private boolean connected = false;
        private InputStream stream;

        public SparqlHttpRequest(String serviceUrl, Query query) throws IOException {

            // Assemble the URL for the HTTP GET operation
            char delimiter = serviceUrl.indexOf('?') == -1 ? '?' : '&';
            String urlString = serviceUrl + delimiter + "query=" + URLEncoder.encode(query.toString(), StandardCharsets.UTF_8);
            URL url = new URL(urlString);

            // Obtain an HttpURLConnection object for the URL
            conn = (HttpURLConnection) url.openConnection();

            // Configure and return the HttpURLConnection object
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/sparql-results+xml");
            conn.setDefaultUseCaches(false);
            conn.setUseCaches(false);
            conn.setDoOutput(false);
        }

        public InputStream submit(long timeoutTs) throws IOException, InterruptedException {
            try {
                // Issue the HTTP request and mark as 'connected' so to ensure 'disconnect' will be called on close
                conn.setConnectTimeout(timeoutTs <= 0 ? 0 : (int) (timeoutTs - System.currentTimeMillis()));
                conn.connect();
                connected = true;

                // Check server response code
                conn.setReadTimeout(timeoutTs <= 0 ? 0 : (int) (timeoutTs - System.currentTimeMillis()));
                int rc = conn.getResponseCode();
                if (rc < 200 || rc >= 300) {
                    throw new IOException("Received error code " + rc + " from server with message: " + conn.getResponseMessage());
                }

                // Obtain and return response body stream (this operation may fail)
                stream = conn.getInputStream();
                return stream;

            } catch (SocketTimeoutException e) {
                // Convert and propagate
                throw (InterruptedException) new InterruptedException().initCause(e);
            }
        }

        public synchronized void close() {

            // Exhaust (to avoid server side errors) and close the stream, invalidating it
            if (stream != null) {
                try {
                    try {
                        while (stream.read() != -1) {
                            //noinspection ResultOfMethodCallIgnored
                            stream.skip(1024 * 1024L);
                        }
                    } finally {
                        stream.close();
                    }
                } catch (Throwable ex) {
                    // ignore
                }
                stream = null;
            }

            // Disconnect if needed and mark as not connected
            if (connected) {
                try {
                    conn.disconnect();
                } catch (Throwable ex) {
                    // ignore
                }
                connected = false;
            }
        }

    }

    private static class SparqlHttpResponseHandler extends DefaultHandler {

        private final Handler handler;
        private final StringBuilder currentText = new StringBuilder(1024);
        private boolean processText;
        private String currentVariable;
        private String currentDatatype;
        private String currentLang;
        private int numSolutions;

        public SparqlHttpResponseHandler(Handler handler) {
            this.handler = handler;
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
