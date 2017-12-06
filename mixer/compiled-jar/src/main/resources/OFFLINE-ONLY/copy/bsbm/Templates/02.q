PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3
 ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4 
WHERE {
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> rdfs:label ?label .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> rdfs:comment ?comment .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:producer ?p .
    ?p rdfs:label ?producer .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> dc:publisher ?p .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productFeature ?f .
    ?f rdfs:label ?productFeature .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyTextual1 ?propertyTextual1 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyTextual2 ?propertyTextual2 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyTextual3 ?propertyTextual3 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyNumeric1 ?propertyNumeric1 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyNumeric2 ?propertyNumeric2 .
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyTextual4 ?propertyTextual4 }
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyTextual5 ?propertyTextual5 }
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer$/Product$> bsbm:productPropertyNumeric4 ?propertyNumeric4 }
}
