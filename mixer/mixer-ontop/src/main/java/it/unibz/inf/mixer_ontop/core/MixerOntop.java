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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
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

public class MixerOntop extends Mixer {

  private static Logger log = LoggerFactory.getLogger(MixerOntop.class);

  private OWLOntology ontology;
  private OntopOWLReasoner reasoner;
  private long rewritingTime;
  private long unfoldingTime;
  private int rewritingSize;
  private int unfoldingSize;
  private OntopOWLConnection conn;
  int subQuery;

  public MixerOntop(Conf configuration) {
    super(configuration);

    ontology = null;
    reasoner = null;
    rewritingTime = 0;
    unfoldingTime = 0;
    rewritingSize = 0;
    unfoldingSize = 0;
    conn = null;
    subQuery = 0;
  }

  @Override
  public void load() throws Exception {
    createReasoner();
  }


  @Override
  public Object executeQuery(String query, int timeout) {
    // TODO handle timeout
    TupleOWLResultSet rs = null;
    try {
      if (conn == null) conn = reasoner.getConnection(); // Warn: this methods will return always
      //       the same connection
      OntopOWLStatement st = conn.createStatement();
      log.debug("Davide> Executing Query:" + query);
      rs = st.executeSelectQuery(query);

//      if (st.getBenchmarkObject() != null) {
//        this.rewritingTime = st.getBenchmarkObject().getRewritingTime();
//        this.unfoldingTime = st.getBenchmarkObject().getUnfoldingTime();
//        this.rewritingSize = st.getBenchmarkObject().getUCQSizeAfterRewriting();
//        this.unfoldingSize = st.getBenchmarkObject().getUCQSizeAfterUnfolding();
//      }
    } catch (OWLException e) {
      e.printStackTrace();
    }
    return rs;
  }

  @Override
  public int traverseResultSet(Object resultSet) {
    if (resultSet == null) return 0;
    TupleOWLResultSet rs = (TupleOWLResultSet) resultSet;
    int resultsCount = 0;
    try {
      int columnSize = rs.getColumnCount();
      // Traverse the result set
      while (rs.hasNext()) {
        final OWLBindingSet bindingSet = rs.next();
        if (!(bindingSet == null)) {
          ++resultsCount;
        }
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
    this.rewriting = false;
  }

  @Override
  public void rewritingON() {
    this.rewriting = true;
  }

  // ---- PRIVATE INTERFACE ---- //

  private void createReasoner() throws Exception {

    OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
    OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
            .propertyFile(configuration.getPropertyFile())
            .nativeOntopMappingFile(configuration.getMappingsFile())
            .ontologyFile(configuration.getOwlFile())
            .enableTestMode()
            .build();
    OntopOWLReasoner reasoner = factory.createReasoner(config);

    this.reasoner = reasoner;
  }

  private void loadOntology() {
    try {
      log.debug("Loading the ontology");
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      this.ontology = manager.loadOntologyFromOntologyDocument(new File(this.configuration.getOwlFile()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void executeWarmUpQuery(String query, int timeout) {
    executeQuery(query, timeout);
  }
};