# Query Templates Syntax

A SPARQL template query is a SPARQL query with placeholders of the form

~~~~~
${id:source:quoting}
~~~~~

where `id` is an integer, source is a string of the form `table_name.col_name`, and `quoting` is one of `{none, underscore, percent}`. During the test phase, `obda-mixer` replaces the placeholders in the provided SPARQL templates to database values retrieved from the column `table_name.col_name` in the database. Blank spaces in such values are encoded according to the `quoting` policy. For instance, a value `Little Color` is kept as is if the `none` quoting is prescribed, is transformed into `Little_Color` if the `underscore` quoting is prescribed, and is transformed into `Little%20Color` if the `percent` quoting is prescribed. If _p1, p2_ are two placeholders retrieving values from different columns in the same table, then the values for _p1, p2_ will be retrieved from the same row. If _p1, p2_ are two placeholders having the same _source_ part and _id_ part, then they will be replaced by the same value. 

We now provide an example. Consider the following query _08.q_ from the [NPD Benchmark](NPD Benchmark):

~~~~~~~~
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>

SELECT *
WHERE {
  [ npdv:productionYear ?year ;
    npdv:productionMonth ?m ;
    npdv:producedGas     ?g ;
    npdv:producedOil     ?o 
 ]
 FILTER (?year > ${1:field_production_totalt_NCS_year.prfYear}) 
 FILTER(?m >= ${1:field_production_totalt_NCS_month.prfMonth} && ?m <= ${2:field_production_totalt_NCS_month.prfMonth} )
} 
~~~~~~~~

Observe that the query contains three placeholders in the FILTER conditions. 

`obda-mixer` instantiates the three placeholders with values retrieved from the columns `field_production_totalt_NCS_month.[prfYear, month]`; Observe that the second and the third placeholders will be instantiated to different values, although they have a common _source part_, since their _id_ part differs. A possible instantiation  of the query template above is:

~~~
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
~~~
