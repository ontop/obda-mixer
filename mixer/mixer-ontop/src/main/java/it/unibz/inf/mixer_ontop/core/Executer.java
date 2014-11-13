package it.unibz.inf.mixer_ontop.core;

import org.semanticweb.owlapi.model.OWLException;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

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
		try {
			if(conn == null) conn = reasoner.getConnection();
			QuestOWLStatement st = conn.createStatement();
			rs = st.executeTuple(query);
			this.rewritingTime = st.getRewritingTime();
			this.unfoldingTime = st.getUnfoldingTime();
			
			this.rewritingSize = st.getUCQSizeAfterRewriting();
			this.unfoldingSize = st.getUCQSizeAfterUnfolding();
			
		} catch (OBDAException | OWLException e) {
			e.printStackTrace();
		} 
		this.result = rs;
    }
}
