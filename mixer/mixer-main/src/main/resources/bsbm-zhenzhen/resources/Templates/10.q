PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX iso3166: <http://downlode.org/rdf/iso-3166/countries#>

SELECT DISTINCT ?offer ?product
WHERE {
        ?offer bsbm:product ?product .
        ?product bsbm:productId ?id .
        FILTER (?id < ${1:product.nr:none})
        ?offer bsbm:vendor ?vendor .
        ?offer dc:publisher ?vendor .
        ?vendor bsbm:country iso3166:${1:vendor.country:none} .
        ?offer bsbm:deliveryDays ?deliveryDays .
        FILTER (?deliveryDays <= 3)
        ?offer bsbm:price ?price .
        ?offer bsbm:validTo ?date .
#       FILTER (?date > "${2:offer.validto:none}" )
}

## Eliminated filter, for avoiding empty results