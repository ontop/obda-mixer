package it.unibz.inf.mixer_web.core;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.mixer_web.net.NetQuery;

public class MixerWeb extends Mixer {

    private final String serviceUrl;
    
    public MixerWeb(Conf configuration, String serviceUrl) {
	super(configuration);
	this.serviceUrl = serviceUrl;
    }
    
    @Override
    public void load() {
	// Unsupported
    }

    @Override
    public void executeWarmUpQuery(String query) {
	NetQuery q = new NetQuery(this.serviceUrl, query, 0);
	q.exec();
	q.close();
    }

    @Override
    public void executeWarmUpQuery(String query, int timeout) {
	NetQuery q = new NetQuery(this.serviceUrl, query, timeout * 1000);
	q.exec();
	q.close();
    }
    
    @Override
    public Object executeQuery(String query) {
	return executeQuery(query, 0);
    }

    @Override
    public Object executeQuery(String query, int timeout) {
	
	NetQuery q = new NetQuery(this.serviceUrl, query, timeout*1000);
	InputStream resultStream = q.exec();
	
	return new ResultSet(q, resultStream);
    }

    @Override
    public int traverseResultSet(Object resultSet) {
	int result = 0;
	InputStream resultStream = ((ResultSet)resultSet).getResultStream();
	try {
	    result = countResults(resultStream);
	    ((ResultSet)resultSet).getQuery().close();
	} catch (SocketTimeoutException e) {
	    e.printStackTrace();
	}
	return result;
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
    
//    private static String convertStreamToString(java.io.InputStream is) {
//	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//	return s.hasNext() ? s.next() : "";
//    }
    
    private int countResults(InputStream s) throws SocketTimeoutException {
	
	class ResultHandler extends DefaultHandler {
	    private int count;
	    ResultHandler() {
		count = 0;
	    }
	    public void startElement( String namespaceURI,
		    String localName,   // local name
		    String qName,       // qualified name
		    Attributes attrs ) {
		if(qName.equals("result"))
		    count++;
	    }
	    public int getCount() {
		return count;
	    }
	};
	
	ResultHandler handler = new ResultHandler();
	int count=0;
	try {
	    SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
//	    ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes("UTF-8"));
	    saxParser.parse( s, handler );
	    count = handler.getCount();
	} catch(SocketTimeoutException e) { throw new SocketTimeoutException(); }
	catch(Exception e) {
	    System.err.println("SAX Error");
	    e.printStackTrace();
	    return -1;
	}
	return count;
    }
    
    private class ResultSet{
	private NetQuery query;
	private InputStream resultStream;
	
	ResultSet(NetQuery query, InputStream resultStream){
	    this.query = query;
	    this.resultStream = resultStream;
	}
	
	NetQuery getQuery(){
	    return this.query;
	}
	
	InputStream getResultStream(){
	    return this.resultStream;
	}
    }
}
