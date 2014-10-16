package it.unibz.inf.mixer_ontop_v2.core;

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
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

public class MixerOntop extends Mixer {
	
	private static Logger log = LoggerFactory.getLogger(MixerOntop.class);

	
	private OBDAModel obdaModel;
	private OWLOntology ontology;
	private QuestOWL reasoner;
	private long rewritingTime;
	private long unfoldingTime;
	private QuestOWLConnection conn;

	public MixerOntop(Conf configuration) {
		super(configuration);
		
		obdaModel = null;
		ontology = null;
		reasoner = null;
		rewritingTime = 0;
		unfoldingTime = 0;
		conn = null;
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
			if(conn == null) conn = new QuestOWLConnection(reasoner.getQuestInstance().getConnection());
			QuestOWLStatement st = conn.createStatement();
			rs = st.executeTuple(query);
			this.rewritingTime = st.getQuestStatement().getRewritingTime();
			this.unfoldingTime = st.getQuestStatement().getUnfoldingTime();
			
		} catch (OBDAException | OWLException e) {
			e.printStackTrace();
		} 
		return rs;
	}

	@Override
	public Object executeQuery(String query, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int traverseResultSet(Object resultSet) {
		
		QuestOWLResultSet rs = (QuestOWLResultSet) resultSet;
		int resultsCount = 0;
		try {
			int columnSize = rs.getColumCount();
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
	public void rewritingOFF() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rewritingON() {
		// TODO Auto-generated method stub
		
	}

	
	// PRIVATE INTERFACE
	
	private void createReasoner() {

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		QuestPreferences preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		
//		preference.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
//		preference.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);
		
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
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
		} catch (IOException | InvalidPredicateDeclarationException
				| InvalidMappingException e) {
			e.printStackTrace();
		}
		
	}
	
}
