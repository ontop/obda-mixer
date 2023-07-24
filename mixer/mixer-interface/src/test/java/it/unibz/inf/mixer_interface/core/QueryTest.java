package it.unibz.inf.mixer_interface.core;

import org.junit.Assert;
import org.junit.Test;

public class QueryTest {

    @Test
    public void test() {
        Assert.assertFalse(Query.builder("SELECT * { ?s ?p ?o }").build().isResultSorted());
        Assert.assertFalse(Query.builder("# mixer:sorted=false\nSELECT * { ?s ?p ?o }").build().isResultSorted());
        Assert.assertTrue(Query.builder("# mixer:sorted=true\nSELECT * { ?s ?p ?o }").build().isResultSorted());
    }

}
