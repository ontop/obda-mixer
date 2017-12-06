INSERT A QUERY
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wc 
		   WHERE { 
		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. 
		   }
!!
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wc 
		   WHERE { 
		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. 
		   }

INSERT A LABEL
qr
11:51:20.453 [main] DEBUG o.o.query.parser.QueryParserRegistry - Registered service class org.openrdf.query.parser.sparql.SPARQLParserFactory
11:51:20.567 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Executing SPARQL query: 
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wc 
		   WHERE { 
		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. 
		   }

11:51:20.574 [Thread-1] DEBUG i.u.k.o.o.c.t.SparqlAlgebraToDatalogTranslator - SPARQL algebra: 
Distinct
   Projection
      ProjectionElemList
         ProjectionElem "wc"
      Join
         StatementPattern
            Var (name=-anon-1, anonymous)
            Var (name=-const-http://www.w3.org/1999/02/22-rdf-syntax-ns#type-uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
            Var (name=-const-http://sws.ifi.uio.no/vocab/npd-v2#Wellbore-uri, value=http://sws.ifi.uio.no/vocab/npd-v2#Wellbore, anonymous)
         StatementPattern
            Var (name=wc)
            Var (name=-const-http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore-uri, value=http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore, anonymous)
            Var (name=-anon-1, anonymous)

11:51:20.577 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Datalog program translated from the SPARQL query: 
ans1(wc) :- ans2(-anon-1,wc)
ans2(-anon-1,wc) :- ans4(-anon-1), ans5(-anon-1,wc)
ans4(-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#Wellbore(-anon-1)
ans5(-anon-1,wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)

11:51:20.581 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Flattened program: 
ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#Wellbore(-anon-1), http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)

11:51:20.582 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Replacing equivalences...
11:51:20.582 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Normalized program: 
ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#Wellbore(-anon-1), http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)

11:51:20.582 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Start the rewriting process...
11:51:20.589 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - CONNECTED COMPONENT ([wc]) EXISTS [loop: {-anon-1}[]]
11:51:20.591 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -      WITH EDGES [edge: {wc, -anon-1}[http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)][][]] AND LOOP null
11:51:20.591 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -      NON-DL ATOMS []
11:51:20.599 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet - QUANTIFIED VARIABLE -anon-1
11:51:20.601 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet - EDGE edge: {wc, -anon-1}[http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)][][] HAS PROPERTY http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)
11:51:20.601 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet - CHECKING WHETHER THE FOLDING Query Folding: [loop: {wc}[]], internal roots [] and domain: [-anon-1] with properties: [http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore, ER.A-AUXROLE17] CAN BE GENERATED: 
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE1
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE0
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForCompany
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForField
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#includedInField
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#exactMatch
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceLicenceeCompany^-
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForDiscovery^-
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#transferredLicence
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellOperator^-
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#fieldOperator
11:51:20.602 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#taskForCompany^-
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE15
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE16
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE13
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#drillingOperatorCompany
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE14
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE11
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE12
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellOperator
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE10
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOwnerCompany
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE19
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#fieldLicensee^-
11:51:20.603 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       POSITIVE PROPERTY CHECK ER.A-AUXROLE17
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -         ENDTYPE IS FINE: TOP FOR tw-generator EER.A-AUXROLE17.http://www.w3.org/2002/07/owl#Thing
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -         OK
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE18
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForLicence^-
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#LOTForWellbore
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE4
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE2
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE3
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE8
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE9
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE6
11:51:20.604 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#semanticRelation^-
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE7
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentResponsibleCompany^-
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#belongsToFacility
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceeForLicence
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#closeMatch
11:51:20.605 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#drillingOperatorCompany^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reclassedFromWellbore
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForField^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#investmentForField^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#stratumForWellbore^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#containsWellbore
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#mudTestForWellbore^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator^-
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForField
11:51:20.606 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coordinateForSurvey^-
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaTransferCompany
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOwner
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceLicensee^-
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#resourcesIncludedInDiscovery^-
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#mudTestForWellbore
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#stratumForWellbore
11:51:20.607 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#LOTForWellbore^-
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForSurvey
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore^-
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator^-
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#corePhotoForWellbore
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#shallowWellboreForLicence
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reportingCompany^-
11:51:20.608 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOwnerCompany^-
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#relatedMatch
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForSurvey^-
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForCompany^-
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reclassedFromWellbore^-
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#includedInField^-
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceLicensee
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellboreForDiscovery
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#country
11:51:20.609 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaOperatorCompany^-
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFLicenceeCompany^-
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForLicence
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForLicence
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOperatorCompany^-
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOwner^-
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOperatorForLicence^-
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#currentResponsibleCompany
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coordinateForWellbore
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#inLithostratigraphicUnit
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#documentURL
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceTransferCompany
11:51:20.610 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceLicenceeCompany
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#oilSampleTestForWellbore^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#lastOperatorCompany^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#registeredInCountry
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceOperatorCompany
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#stratigraphicParent
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#belongsToFacility^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#developmentWellboreForField^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceeForLicence^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#fieldOperator^-
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaLicensee
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#DSTForWellbore
11:51:20.611 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellboreForDiscovery^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#provinceLocation
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#developmentWellboreForLicence
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#fieldLicensee
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#exactMatch^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#ownerForField
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#statusForLicence^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#isGeometryOfFeature
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#semanticRelation
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#oilSampleTestForWellbore
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#messageForLicence^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coordinateForWellbore^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceOperatorCompany^-
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#belongsToWell
11:51:20.612 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#operatorForLicence
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForBAA^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForField^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#related
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#taskForLicence
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reservesForDiscovery
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#pipelineToFacility
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#isGeometryOfFeature^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#ownerForField^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaTransferCompany^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#geochronologicEra
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#developmentWellboreForLicence^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#documentForWellbore^-
11:51:20.613 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFLicenceeForTUF
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE72
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE71
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOwnerForLicence
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOperatorForLicence
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#transferredLicence^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#related^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE70
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#closeMatch^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#DSTForWellbore^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#operatorForField
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOwnerForLicence^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#blockLocation
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#taskForCompany
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceTransferCompany^-
11:51:20.614 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFLicenceeCompany
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#investmentForField
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE63
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE62
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#corePhotoForWellbore^-
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE61
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE60
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE68
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#operatorForField^-
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE69
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE66
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE67
11:51:20.615 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE64
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#factMapURL
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE65
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#npdPageURL
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenceOperatorCompany^-
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#drillingFacility
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE50
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE51
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE52
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       POSITIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -         ENDTYPE IS FINE: TOP FOR tw-generator Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore.http://www.w3.org/2002/07/owl#Thing
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -         OK
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForBAA
11:51:20.616 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coordinateForSurvey
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#transferredBAA
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#pipelineFromFacility
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#documentForWellbore
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE54
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE53
11:51:20.617 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE56
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE55
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#licenceOperatorCompany
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaLicensee^-
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE58
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE57
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore^-
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE59
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#quadrantLocation
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#resourcesIncludedInDiscovery
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#corePhotoURL
11:51:20.618 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#coresForWellbore
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#messageForTUF
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE40
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE41
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#lastOperatorCompany
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFLicenceeForTUF^-
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#messageForTUF^-
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE45
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE44
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE43
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE42
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE49
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE48
11:51:20.619 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE47
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE46
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE30
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFOperatorCompany
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForField^-
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://www.w3.org/2004/02/skos/core#relatedMatch^-
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#factPageURL
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#transferredBAA^-
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence^-
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE39
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE36
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE35
11:51:20.620 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE38
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE37
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE32
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE31
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE34
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE33
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#reportingCompany
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellboreForField^-
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#licenseeForField
11:51:20.621 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#shallowWellboreForLicence^-
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#taskForLicence^-
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#wellboreForField
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#mainAreaLocation
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#operatorForLicence^-
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE29
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#baaOperatorCompany
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE28
11:51:20.622 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#developmentWellboreForField
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE27
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE26
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE25
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE24
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE23
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK http://sws.ifi.uio.no/vocab/npd-v2#messageForLicence
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE22
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE21
11:51:20.623 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet -       NEGATIVE PROPERTY CHECK ER.A-AUXROLE20
11:51:20.624 [Thread-1] DEBUG i.u.k.o.o.c.r.QueryFolding - NEW TREE WITNESS
11:51:20.624 [Thread-1] DEBUG i.u.k.o.o.c.r.QueryFolding -   PROPERTIES [http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore, ER.A-AUXROLE17]
11:51:20.624 [Thread-1] DEBUG i.u.k.o.o.c.r.QueryFolding -   ENDTYPE TOP
11:51:20.624 [Thread-1] DEBUG i.u.k.o.o.c.r.QueryFolding -   NOT MERGEABLE: loop: {wc}[] IS NOT QUANTIFIED
11:51:20.624 [Thread-1] DEBUG i.u.k.o.o.c.r.QueryFolding -   ROOTTYPE []
11:51:20.627 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessSet - TREE WITNESSES FOUND: 1
11:51:20.628 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - TREE WITNESS: tree witness generated by [tw-generator EER.A-AUXROLE17.http://www.w3.org/2002/07/owl#Thing, tw-generator Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore.http://www.w3.org/2002/07/owl#Thing]
    with domain tree witness domain [wc, -anon-1] with roots [wc] and root atoms []
11:51:20.628 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessGenerator - MORE THAN ONE GENERATING CONCEPT: [Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreIntervalBottom, http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore, http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet, Ehttp://sws.ifi.uio.no/vocab/npd-v2#isCoreSampleAvailable, EER.A-AUXROLE17, Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreIntervalTop]
11:51:20.633 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreIntervalBottom
11:51:20.634 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore
11:51:20.634 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet
11:51:20.635 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: Ehttp://sws.ifi.uio.no/vocab/npd-v2#isCoreSampleAvailable
11:51:20.635 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: EER.A-AUXROLE17
11:51:20.635 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter -   BASIC CONCEPT: Ehttp://sws.ifi.uio.no/vocab/npd-v2#coreIntervalTop
11:51:20.636 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - EDGE edge: {wc, -anon-1}[http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)][][]
11:51:20.636 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - REWRITTEN PROGRAM
[ans1(wc) :- ans1_EDGE_1(wc,-anon-1)]CC DEFS
null
11:51:20.636 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - EDGE DEFS
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,-anon-1)
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalBottom(wc,_)
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore(wc)
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet(wc)
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#isCoreSampleAvailable(wc,_)
ans1_EDGE_1(wc,-anon-1) :- ER.A-AUXROLE17(wc,_)
ans1_EDGE_1(wc,-anon-1) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalTop(wc,_)

11:51:20.647 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,_)
11:51:20.648 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalTop(wc,_)
11:51:20.648 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- ER.A-AUXROLE17(wc,_)
11:51:20.649 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#isCoreSampleAvailable(wc,_)
11:51:20.649 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet(wc)
11:51:20.649 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore(wc)
11:51:20.649 [Thread-1] DEBUG i.u.k.o.o.c.r.DatalogQueryServices - ADDING TO THE RESULT ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalBottom(wc,_)
11:51:20.649 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - INLINE EDGE PROGRAM
[ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,_), ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalTop(wc,_), ans1(wc) :- ER.A-AUXROLE17(wc,_), ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#isCoreSampleAvailable(wc,_), ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet(wc), ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore(wc), ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreIntervalBottom(wc,_)]CC DEFS
null
11:51:20.652 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - Rewriting time: 0.066 s (total 0.146 s)
11:51:20.652 [Thread-1] DEBUG i.u.k.o.o.c.r.TreeWitnessRewriter - Final rewriting:
ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#coreForWellbore(wc,_)
ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet(wc)
ans1(wc) :- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore(wc)

11:51:20.652 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Start the partial evaluation process...
11:51:20.654 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Data atoms evaluated: 
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t10_8365,t6_8365)) :- strat_litho_wellbore_core(t1_8365,t2_8365,t3_8365,t4_8365,t5_8365,t6_8365,t10_8365,t8_8365), wellbore_npdid_overview(t9_8365,t10_8365,t11_8365,t12_8365,t13_8365), IS_NOT_NULL(t10_8365), IS_NOT_NULL(t6_8365)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t14_8364,t2_8364)) :- wellbore_core(t1_8364,t2_8364,t3_8364,t4_8364,t5_8364,t6_8364,t7_8364,t8_8364,t14_8364,t10_8364,t11_8364,t12_8364), wellbore_npdid_overview(t13_8364,t14_8364,t15_8364,t16_8364,t17_8364), IS_NOT_NULL(t14_8364), IS_NOT_NULL(t2_8364)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t7_8366,t6_8366)) :- strat_litho_wellbore_core(t1_8366,t2_8366,t3_8366,t4_8366,t5_8366,t6_8366,t7_8366,t8_8366), IS_NOT_NULL(t7_8366), IS_NOT_NULL(t6_8366)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8369,t3_8369)) :- view_1(t1_8369,t2_8369,t3_8369,t4_8369), IS_NOT_NULL(t2_8369), IS_NOT_NULL(t3_8369)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8368,t3_8368)) :- view_0(t1_8368,t2_8368,t3_8368,t4_8368), IS_NOT_NULL(t2_8368), IS_NOT_NULL(t3_8368)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t9_8367,t2_8367)) :- wellbore_core(t1_8367,t2_8367,t3_8367,t4_8367,t5_8367,t6_8367,t7_8367,t8_8367,t9_8367,t10_8367,t11_8367,t12_8367), IS_NOT_NULL(t9_8367), IS_NOT_NULL(t2_8367)

11:51:20.654 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Irrelevant rules removed: 
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t10_8365,t6_8365)) :- strat_litho_wellbore_core(t1_8365,t2_8365,t3_8365,t4_8365,t5_8365,t6_8365,t10_8365,t8_8365), wellbore_npdid_overview(t9_8365,t10_8365,t11_8365,t12_8365,t13_8365), IS_NOT_NULL(t10_8365), IS_NOT_NULL(t6_8365)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t14_8364,t2_8364)) :- wellbore_core(t1_8364,t2_8364,t3_8364,t4_8364,t5_8364,t6_8364,t7_8364,t8_8364,t14_8364,t10_8364,t11_8364,t12_8364), wellbore_npdid_overview(t13_8364,t14_8364,t15_8364,t16_8364,t17_8364), IS_NOT_NULL(t14_8364), IS_NOT_NULL(t2_8364)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t7_8366,t6_8366)) :- strat_litho_wellbore_core(t1_8366,t2_8366,t3_8366,t4_8366,t5_8366,t6_8366,t7_8366,t8_8366), IS_NOT_NULL(t7_8366), IS_NOT_NULL(t6_8366)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8369,t3_8369)) :- view_1(t1_8369,t2_8369,t3_8369,t4_8369), IS_NOT_NULL(t2_8369), IS_NOT_NULL(t3_8369)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8368,t3_8368)) :- view_0(t1_8368,t2_8368,t3_8368,t4_8368), IS_NOT_NULL(t2_8368), IS_NOT_NULL(t3_8368)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t9_8367,t2_8367)) :- wellbore_core(t1_8367,t2_8367,t3_8367,t4_8367,t5_8367,t6_8367,t7_8367,t8_8367,t9_8367,t10_8367,t11_8367,t12_8367), IS_NOT_NULL(t9_8367), IS_NOT_NULL(t2_8367)

11:51:20.659 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Boolean expression evaluated: 
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t10_8365,t6_8365)) :- strat_litho_wellbore_core(t1_8365,t2_8365,t3_8365,t4_8365,t5_8365,t6_8365,t10_8365,t8_8365), wellbore_npdid_overview(t9_8365,t10_8365,t11_8365,t12_8365,t13_8365), IS_NOT_NULL(t10_8365), IS_NOT_NULL(t6_8365)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t14_8364,t2_8364)) :- wellbore_core(t1_8364,t2_8364,t3_8364,t4_8364,t5_8364,t6_8364,t7_8364,t8_8364,t14_8364,t10_8364,t11_8364,t12_8364), wellbore_npdid_overview(t13_8364,t14_8364,t15_8364,t16_8364,t17_8364), IS_NOT_NULL(t14_8364), IS_NOT_NULL(t2_8364)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t7_8366,t6_8366)) :- strat_litho_wellbore_core(t1_8366,t2_8366,t3_8366,t4_8366,t5_8366,t6_8366,t7_8366,t8_8366), IS_NOT_NULL(t7_8366), IS_NOT_NULL(t6_8366)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8369,t3_8369)) :- view_1(t1_8369,t2_8369,t3_8369,t4_8369), IS_NOT_NULL(t2_8369), IS_NOT_NULL(t3_8369)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t2_8368,t3_8368)) :- view_0(t1_8368,t2_8368,t3_8368,t4_8368), IS_NOT_NULL(t2_8368), IS_NOT_NULL(t3_8368)
ans1(URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",t9_8367,t2_8367)) :- wellbore_core(t1_8367,t2_8367,t3_8367,t4_8367,t5_8367,t6_8367,t7_8367,t8_8367,t9_8367,t10_8367,t11_8367,t12_8367), IS_NOT_NULL(t9_8367), IS_NOT_NULL(t2_8367)

11:51:20.659 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Partial evaluation ended.
11:51:20.660 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Producing the SQL string...
11:51:20.670 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Resulting SQL: 
SELECT *
FROM (
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/stratum/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`lsuNpdidLithoStrat` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/cores') AS `wc`
 FROM 
strat_litho_wellbore_core QVIEW1,
wellbore_npdid_overview QVIEW2
WHERE 
(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`lsuNpdidLithoStrat` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbCoreNumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
wellbore_core QVIEW1,
wellbore_npdid_overview QVIEW2
WHERE 
(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`wlbCoreNumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/stratum/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`lsuNpdidLithoStrat` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/cores') AS `wc`
 FROM 
strat_litho_wellbore_core QVIEW1
WHERE 
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`lsuNpdidLithoStrat` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbnpdidwellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbcorenumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
(SELECT wellbore_core_id, wlbNpdidWellbore, wlbCoreNumber, wlbCoreIntervalTop * 0.3048 AS wlbCoreIntervalTopFT FROM wellbore_core WHERE wlbCoreIntervalUom = '[ft  ]') QVIEW1
WHERE 
QVIEW1.`wlbnpdidwellbore` IS NOT NULL AND
QVIEW1.`wlbcorenumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbnpdidwellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbcorenumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
(SELECT wellbore_core_id, wlbNpdidWellbore, wlbCoreNumber, wlbCoreIntervalBottom * 0.3048 AS wlbCoreIntervalBottomFT FROM wellbore_core WHERE wlbCoreIntervalUom = '[ft  ]') QVIEW1
WHERE 
QVIEW1.`wlbnpdidwellbore` IS NOT NULL AND
QVIEW1.`wlbcorenumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbCoreNumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
wellbore_core QVIEW1
WHERE 
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`wlbCoreNumber` IS NOT NULL
) SUB_QVIEW

11:51:20.671 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Executing the SQL query and get the result...
11:51:20.980 [Thread-1] DEBUG i.u.k.o.o.core.QuestStatement - Execution finished.


The input SPARQL query:
=======================
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wc 
		   WHERE { 
		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. 
		   }


The output SQL query:
=====================
SELECT *
FROM (
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/stratum/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`lsuNpdidLithoStrat` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/cores') AS `wc`
 FROM 
strat_litho_wellbore_core QVIEW1,
wellbore_npdid_overview QVIEW2
WHERE 
(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`lsuNpdidLithoStrat` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbCoreNumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
wellbore_core QVIEW1,
wellbore_npdid_overview QVIEW2
WHERE 
(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`wlbCoreNumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/stratum/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`lsuNpdidLithoStrat` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/cores') AS `wc`
 FROM 
strat_litho_wellbore_core QVIEW1
WHERE 
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`lsuNpdidLithoStrat` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbnpdidwellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbcorenumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
(SELECT wellbore_core_id, wlbNpdidWellbore, wlbCoreNumber, wlbCoreIntervalTop * 0.3048 AS wlbCoreIntervalTopFT FROM wellbore_core WHERE wlbCoreIntervalUom = '[ft  ]') QVIEW1
WHERE 
QVIEW1.`wlbnpdidwellbore` IS NOT NULL AND
QVIEW1.`wlbcorenumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbnpdidwellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbcorenumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
(SELECT wellbore_core_id, wlbNpdidWellbore, wlbCoreNumber, wlbCoreIntervalBottom * 0.3048 AS wlbCoreIntervalBottomFT FROM wellbore_core WHERE wlbCoreIntervalUom = '[ft  ]') QVIEW1
WHERE 
QVIEW1.`wlbnpdidwellbore` IS NOT NULL AND
QVIEW1.`wlbcorenumber` IS NOT NULL
UNION
SELECT 
   1 AS `wcQuestType`, NULL AS `wcLang`, CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbNpdidWellbore` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'), '/core/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`wlbCoreNumber` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `wc`
 FROM 
wellbore_core QVIEW1
WHERE 
QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND
QVIEW1.`wlbCoreNumber` IS NOT NULL
) SUB_QVIEW