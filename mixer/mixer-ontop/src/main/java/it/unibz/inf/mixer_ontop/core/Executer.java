package it.unibz.inf.mixer_ontop.core;

import org.semanticweb.owlapi.model.OWLException;

import it.unibz.inf.ontop.owlrefplatform.core.benchmark.OntopBenchmark;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;

public class Executer extends Thread {
	
	private QuestOWLConnection conn;
	private QuestOWL reasoner;
	private long rewritingTime;
	private long unfoldingTime;
	private int rewritingSize;
	private int unfoldingSize;
	public long getRewritingTime() {
		return rewritingTime;
	}

	public long getUnfoldingTime() {
		return unfoldingTime;
	}

	public int getRewritingSize() {
		return rewritingSize;
	}

	public int getUnfoldingSize() {
		return unfoldingSize;
	}

	public QuestOWLResultSet getResult() {
		return result;
	}

	private String query;
	private QuestOWLResultSet result;
	
	
	public Executer(QuestOWLConnection conn, QuestOWL reasoner, String query){
		this.conn = conn;
		this.reasoner = reasoner;
		this.rewritingSize = 0;
		this.rewritingTime = 0;
		this.unfoldingSize = 0;
		this.unfoldingTime = 0;
		this.query = query;
		
	}
	
	public void run() {
		 
		QuestOWLResultSet rs = null;
		if(conn == null) conn = reasoner.getConnection();
		
		QuestOWLStatement st;
		try {
		    st = conn.createStatement();
		    rs = st.executeTuple(query);
		} catch (OWLException e) {
		    e.printStackTrace();
		}

		if( OntopBenchmark.getInstance() != null ){
		
		    this.rewritingTime = OntopBenchmark.getInstance().getRewritingTime();
		    this.unfoldingTime = OntopBenchmark.getInstance().getUnfoldingTime();
		
		    this.rewritingSize = OntopBenchmark.getInstance().getUCQSizeAfterRewriting();
		    this.unfoldingSize = OntopBenchmark.getInstance().getUCQSizeAfterUnfolding();
		    
		    this.result = rs;
		}
    }
}
