PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label ?propertyTextual
WHERE 
    { 
       ?product rdfs:label ?label .
       ?product rdf:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType$> .
       ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature$> .
	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature$> .
       ?product bsbm:productPropertyTextual1 ?propertyTextual .
	   ?product bsbm:productPropertyNumeric1 ?p1 .
	   FILTER ( ?p1 > $ )
    } UNION {
       ?product rdfs:label ?label .
       ?product rdf:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType$> .
       ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature$> .
	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature$> .
       ?product bsbm:productPropertyTextual1 $ .
	   ?product bsbm:productPropertyNumeric2 ?p2 .
	   FILTER ( ?p2> $ ) 
    } 
}
ORDER BY ?label
OFFSET 5
LIMIT 10