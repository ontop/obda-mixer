package it.unibz.inf.mixer_ontop.core;

/*
 * #%L
 * mixer-ontop
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.benchmark.OntopBenchmark;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;

public class MixerOntop extends Mixer {

    private static Logger log = LoggerFactory.getLogger(MixerOntop.class);


    private OBDAModel obdaModel;
    private OWLOntology ontology;
    private QuestOWL reasoner;
    private long rewritingTime;
    private long unfoldingTime;
    private int rewritingSize;
    private int unfoldingSize;
    private int sqlCharsNumber;
    private QuestOWLConnection conn;
    int subQuery;

    public MixerOntop(Conf configuration) {
	super(configuration);

	obdaModel = null;
	ontology = null;
	reasoner = null;
	rewritingTime = 0;
	unfoldingTime = 0;
	rewritingSize = 0;
	unfoldingSize = 0;
	this.sqlCharsNumber = 0;
	conn = null;
	subQuery = 0;
    }

    public MixerOntop(Conf configuration, boolean rewriting){
	super(configuration, rewriting);

	obdaModel = null;
	ontology = null;
	reasoner = null;
	rewritingTime = 0;
	unfoldingTime = 0;
	rewritingSize = 0;
	unfoldingSize = 0;
	this.sqlCharsNumber = 0;
	conn = null;
	subQuery = 0;
    }


    @Override
    public void load() {
	loadOntology();	
	loadMappings();		
	createReasoner();
    }



    @Override
    public Object executeQuery(String query) {
	QuestOWLResultSet rs = null;
	try {
	    if(conn == null) conn = reasoner.getConnection(); // Warn: this methods will return always 
	    //       the same connection
	    QuestOWLStatement st = conn.createStatement();
	    rs = st.executeTuple(query);
	    
	    if( OntopBenchmark.getInstance() != null ){
		this.rewritingTime = OntopBenchmark.getInstance().getRewritingTime();
		this.unfoldingTime = OntopBenchmark.getInstance().getUnfoldingTime();
		this.rewritingSize = OntopBenchmark.getInstance().getUCQSizeAfterRewriting(); 
		this.unfoldingSize = OntopBenchmark.getInstance().getUCQSizeAfterUnfolding();
	    }
	} catch ( OWLException e ) {
	    e.printStackTrace();
	} 
	return rs;
    }

    @Override
    public Object executeQuery(String query, int timeout) {
	System.err.println("MixerOntop.executeQuery(String, int) is not implemented yet");
	// TODO 
	return null;
    }

    @Override
    public int traverseResultSet(Object resultSet) {
	if(resultSet == null) return 0;
	QuestOWLResultSet rs = (QuestOWLResultSet) resultSet;
	int resultsCount = 0;
	try {
	    int columnSize = rs.getColumnCount();
	    // Traverse the result set
	    while (rs.nextRow()) {
		for (int idx = 1; idx <= columnSize; idx++) {
		    OWLObject binding = rs.getOWLObject(idx);
		    if( !(binding == null) ){
			++resultsCount;
			//						System.out.print(binding.toString() + ", ");
		    }
		    else{
			//					System.out.println(", ");
		    }
		}
		//			System.out.print("\n");
	    }
	} catch (OWLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return resultsCount;
    }
    @Override
    public long getRewritingTime() {
	return rewritingTime;
    }

    @Override
    public long getUnfoldingTime() {
	return unfoldingTime;
    }

    @Override
    public String getUnfolding() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRewriting() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getUnfoldingSize() {
	return this.unfoldingSize;
    }

    @Override
    public int getRewritingSize() {
	return this.rewritingSize;
    }

    @Override
    public void rewritingOFF() {
	// TODO Auto-generated method stub

    }

    @Override
    public void rewritingON() {
	// TODO Auto-generated method stub

    }

    @Override
    public int getSQLCharsNumber() {
	return this.sqlCharsNumber;
    }


    // PRIVATE INTERFACE

    private void createReasoner() {

	/*
	 * Prepare the configuration for the Quest instance. The example below shows the setup for
	 * "Virtual ABox" mode
	 */
	QuestPreferences preference = new QuestPreferences();
	preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

	if(this.rewriting){
	    preference.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
	    preference.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);
	}

	/*
	 * Create the instance of Quest OWL reasoner.
	 */
	QuestOWLFactory factory = new QuestOWLFactory();
	QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(preference).build();
        this.reasoner = factory.createReasoner(ontology, config);
    }

    private void loadOntology() {
	try{
	    log.debug("Loading the ontology");
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    this.ontology = manager.loadOntologyFromOntologyDocument(new File(this.configuration.getOwlFile()));
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    private void loadMappings() {
	// Load OBDA File

	if( configuration.getMappingsFile().endsWith(".obda") ){
	    loadOBDAMappings();
	}
	else if( configuration.getMappingsFile().endsWith(".ttl") ){
	    loadR2RMLMappings();
	}

    }

    private void loadR2RMLMappings() {
	// TODO
    }

    private void loadOBDAMappings() {
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	this.obdaModel = fac.getOBDAModel();
	ModelIOManager ioManager = new ModelIOManager(obdaModel);

	try {
	    ioManager.load(configuration.getMappingsFile());
	} catch (IOException | InvalidPredicateDeclarationException | InvalidMappingException e) {
	    e.printStackTrace();
	}
    }
};