package it.unibz.inf.mixer.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TemplateTest {

    @Test
    public void test() {
        Template t = new Template("ciao a tut/// ${1:tableName.colName:none}/${1:t2.c2:percent}/");

        assertEquals(2, t.getPlaceholders().size());

        Template.Placeholder p = t.getPlaceholders().get(0);

        assertEquals("1:tableName.colName", p.getName());
        assertEquals(Template.Quoting.NONE, p.getQuoting());
        assertEquals(0, p.getIndex());

        p = t.getPlaceholders().get(1);
        assertEquals("1:t2.c2", p.getName());
        assertEquals(Template.Quoting.PERCENT, p.getQuoting());
        assertEquals(1, p.getIndex());

        String s = t.apply("tappo", "ciao");

        assertEquals("ciao a tut/// tappo/ciao/", s);
    }

}
