package it.unibz.inf.mixer.core;

import org.junit.Assert;
import org.junit.Test;

public class QueryTest {

    @Test
    public void test() {
        Assert.assertFalse(Query.builder(new Template("SELECT * { ?s ?p ?o }")).build().isResultSorted());
        Assert.assertFalse(Query.builder(new Template("# mixer:sorted=false\nSELECT * { ?s ?p ?o }")).build().isResultSorted());
        Assert.assertTrue(Query.builder(new Template("# mixer:sorted=true\nSELECT * { ?s ?p ?o }")).build().isResultSorted());
    }

}
