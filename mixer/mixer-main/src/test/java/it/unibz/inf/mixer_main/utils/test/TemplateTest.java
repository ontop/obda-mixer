package it.unibz.inf.mixer_main.utils.test;

import static org.junit.Assert.*;
import it.unibz.inf.mixer_main.utils.Template;
import it.unibz.inf.mixer_main.utils.Template.PlaceholderInfo;

import org.junit.Test;

public class TemplateTest {

    @Test
    public void test() {
	Template t = new Template("ciao a tut/// ${1:tableName.colName:none}/${1:t2.c2:percent}/");
	
	Template.PlaceholderInfo pI = t.getNthPlaceholderInfo(1);
	
	assertEquals("id = 1, qN = tableName.colName", pI.toString());
	
	pI = t.getNthPlaceholderInfo(2);
	assertEquals("id = 1, qN = t2.c2", pI.toString());
	
	t.setNthPlaceholder(1, "tappo");
	t.setNthPlaceholder(2, "ciao");
	
	assertEquals("ciao a tut/// tappo/ciao/", t.getFilled());	
    }

}
