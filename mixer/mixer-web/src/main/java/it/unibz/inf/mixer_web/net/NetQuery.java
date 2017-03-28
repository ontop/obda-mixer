package it.unibz.inf.mixer_web.net;

import java.net.*;
import java.io.*;

public class NetQuery {
	HttpURLConnection conn;
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
			return conn.getInputStream();
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
	    if (conn != null) 
		conn.disconnect();
	    conn = null;
	}
};
