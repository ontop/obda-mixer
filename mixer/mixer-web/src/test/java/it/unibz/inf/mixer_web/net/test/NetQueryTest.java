package it.unibz.inf.mixer_web.net.test;

import org.junit.Ignore;
import org.junit.Test;
import it.unibz.inf.mixer_web.net.NetQuery;

/**
 * 
 * @author Davide Lanti
 *
 * Test class
 */
public class NetQueryTest{

    private final static String QUERY = "PREFIX swrc: <http://swrc.ontoware.org/ontology#> "
	    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
	    + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
	    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
	    + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
	    + "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
	    + "       SELECT * WHERE {"
	    + "       ?D swrc:journal <http://dblp.l3s.de/d2r/resource/journals/tplp> ."
	    + "       ?D dc:creator ?A."
	    + "       ?A foaf:name ?name."
	    + "       }"
	    + "LIMIT 10";

    private final static String SERVICE_URL = "http://10.7.20.65:2021/sparql";

//    @Ignore
//    @Test
//    public void testNetQuery(){
//	NetQuery q = new NetQuery(SERVICE_URL, QUERY, 1200000);
//
//	String result;
//	result = convertStreamToString(q.exec());
//	System.out.println(result);
//	System.out.println(q.getExecutionTimeInSeconds());
//	q.close();
//    }

    private static String convertStreamToString(java.io.InputStream is) {
	@SuppressWarnings("resource")
	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	return s.hasNext() ? s.next() : "";
    }
};
