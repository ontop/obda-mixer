# obda-mixer v1.2.0

`obda-mixer` is a [maven](http://maven.apache.org/) project born with the intent of testing OBDA systems. This version (`1.2.0`) can interact with the OBDA system through three different modalities:

- `java-api`: To access fine-grained statistics such as _rewriting_ (i.e., the time spent in rewriting the input query before the unfolding phase) or _unfolding time_ (the time spent in unfolding the mappings definitions and producing the SQL translation for the input query). This is achieved by implementing the statistics gathering methods in the abstract class `Mixer`. A default implementation is currently provided for the latest stable version (`v1.18`) of the [Ontop ODA system](https://github.com/ontop/ontop)
- `sparql-endpoint`: To query HTTP SPARQL endpoints; only response times and aggregate statistics based on response times are available in this mode.
- `shell`: To query the OBDA system under test through a command-line interface; limitations for `sparql-endpoint` apply in this mode as well.

## Rationale

`obda-mixer` runs a set of SPARQL queries (_query mix_) over an OBDA system. A typical query is an instance of a [SPARQL query template](Query Templates Syntax), where a SPARQL query template is a SPARQL query with placeholders. Such templates will be instantiated by obda-mixer at runtime with values taken from a database of choice. For instance, a query with placeholders is:

```sparql
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>

SELECT DISTINCT ?licenceURI ?interest ?date
WHERE {
    ?licenceURI a npdv:ProductionLicence .
    		
    [ ] a npdv:ProductionLicenceLicensee ;
      	npdv:dateLicenseeValidFrom ?date ;
      	npdv:licenseeInterest ?interest ;
      	npdv:licenseeForLicence ?licenceURI .   
   FILTER(?date > "${1:licence_licensee_hst.prlLicenseeDateValidFrom:none}"^^xsd:date)	
}
```

In the query above, the placeholder is `${1:licence_licensee_hst.prlLicenseeDateValidFrom:none}`. At runtime, obda-mixer instantiates this placeholder with values from the column `prlLicenseeDateValidFrom` in table `licence_licensee_hst`.

A possible instantiation  of the query template above is:

```sparql
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>

SELECT *
WHERE {
  [ npdv:productionYear ?year ;
    npdv:productionMonth ?m ;
    npdv:producedGas     ?g ;
    npdv:producedOil     ?o 
 ]
 FILTER (?year > 2008) 
 FILTER(?m >= 1 && ?m <= 12 )
} 
```

In this way, it is possible to instantiate the same queries in many different ways, so as to
reduce the impact of caching on the measured execution times (useful for cold-run analyses).

## First Steps

See [Build and Run the Mixer](Build and Run the Mixer).

## Contribute by Extending the Java API Interface!!

The Java API interface is currently implemented for the [Ontop](https://github.com/ontop/ontop) system. 
To implement your own interface for an OBDA system of your choice, refer to [How To Instantiate the Mixer Interface with an OBDA System through the Java API](How To Instantiate the Mixer Interface with an OBDA System through the Java API)
