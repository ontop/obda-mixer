# obda-mixer v1.1.0

The mixer is a [maven](http://maven.apache.org/) project born with the intent of testing OBDA systems. This version (`1.1.0`) interacts with the OBDA system through a Java API interface. Such interface is currently implemented for the latest stable version (`v1.18`) of the [Ontop ODA system](https://github.com/ontop/ontop). Mixer gives access to fine-grained and OBDA-specific metrics such as the_query unfolding time_ (i.e., the time spent in unfolding the mappings definitions and producing the SQL translation for the input query) or the _query rewriting time_ (i.e., the time spent in rewriting the input query so as to take into account for the axioms in the ontology).

## Rationale

OBDA-Mixer runs a set of SPARQL queries (_query mix_) over an OBDA system. A typical query is an instance of a [SPARQL query template](Query Templates Syntax), where a SPARQL query template is a SPARQL query with placeholders. Such templates will be instantiated by obda-mixer at runtime with values taken from a database of choice. For instance, a query with placeholders is:

~~~
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>

SELECT DISTINCT ?licenceURI ?interest ?date
WHERE {
    ?licenceURI a npdv:ProductionLicence .
    		
    [ ] a npdv:ProductionLicenceLicensee ;
      	npdv:dateLicenseeValidFrom ?date ;
      	npdv:licenseeInterest ?interest ;
      	npdv:licenseeForLicence ?licenceURI .   
   FILTER(?date > "${1:licence_licensee_hst.prlLicenseeDateValidFrom}"^^xsd:date)	
}
~~~

In the query above, the placeholder is `${1:licence_licensee_hst.prlLicenseeDateValidFrom}`. At runtime, obda-mixer instantiates this placeholder with values from the column `prlLicenseeDateValidFrom` in table `licence_licensee_hst`.

## First Steps

See [Build and Run the Mixer](Build and Run the Mixer).

## Implementing the Java API Interface

The Java API interface is currently implemented for the [Ontop](https://github.com/ontop/ontop) system. 
To implement your own interface for an OBDA system of your choice, refer to [How To Instantiate the Mixer Interface with an OBDA System through the Java API](How Instantiate the Mixer Interface with an OBDA System through the Java API)
