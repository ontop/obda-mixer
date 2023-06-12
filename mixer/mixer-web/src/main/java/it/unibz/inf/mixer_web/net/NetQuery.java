package it.unibz.inf.mixer_web.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class NetQuery {
	HttpURLConnection conn;
	InputStream stream;
	Long start;
	Long end;
	String queryString;
	
	public NetQuery(String serviceURL, String query, int timeout) {
		String urlString = null;
		try {
			queryString = query;
			char delim=serviceURL.indexOf('?')==-1?'?':'&';
			urlString = serviceURL + delim + "query=" + URLEncoder.encode(query, "UTF-8");
			delim = '&';		
			
			URL url = new URL(urlString);
			conn = (HttpURLConnection)url.openConnection();

			configureConnection(query, timeout);
		} catch(UnsupportedEncodingException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		} catch(MalformedURLException e) {
			System.err.println(e.toString() + " for URL: " + urlString);
			System.err.println(serviceURL);
			e.printStackTrace();
			System.exit(-1);
		} catch(IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void configureConnection(String query, int timeout)
			throws ProtocolException, IOException{
	    
	    conn.setRequestMethod("GET");
	    conn.setDefaultUseCaches(false);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    if( timeout != 0 ){
		conn.setReadTimeout(timeout);
	    }
	    
	    // SELECT Query
	    conn.setRequestProperty("Accept", "application/sparql-results+xml");
	}

	public InputStream exec() {
		try {
		    conn.connect();
		} catch(IOException e) {
			System.err.println("Could not connect to SPARQL Service.");
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			start = System.nanoTime();
			int rc = conn.getResponseCode();
			if(rc < 200 || rc >= 300) {
				System.err.println("Query execution: Received error code " + rc + " from server");
				System.err.println("Error message: " + conn.getResponseMessage() + "\n\nFor query: \n");
				System.err.println(queryString + "\n");
				
			}
			stream = consumeBeforeClose(conn.getInputStream());
			return stream;
		} catch(SocketTimeoutException e) {
			return null;
		} catch(IOException e) {
		    System.err.println("Query execution error:");
		    e.printStackTrace();
		    System.exit(-1);
		    return null;
		}
	}
	
	public double getExecutionTimeInSeconds() {
		end = System.nanoTime();
		Long interval = end-start;
		Thread.yield();
		return interval.doubleValue()/1000000000;
	}
	
	public void close() {
	    if (conn != null) {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ex) {
					System.err.println("Warning: could not properly close SPARQL response stream (" + ex.getMessage() + ")");
				}
				stream = null;
			}
			conn.disconnect();
			conn = null;
		}
	}

	private static InputStream consumeBeforeClose(InputStream stream) {
		return new FilterInputStream(stream) {

			private boolean closed = false;

			@Override
			public void close() throws IOException {
				if (!closed) {
					try {
						while (read() != -1) {
							//noinspection ResultOfMethodCallIgnored
							skip(1024 * 1024L);
						}
					} catch (IOException ex) {
						System.err.println("Warning: could not fully consume SPARQL response (" + ex.getMessage() + ")");
					} finally {
						closed = true;
						super.close();
					}
				}
			}

		};
	}

};
