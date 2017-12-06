PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

CONSTRUCT {  <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:product ?productURI .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:productlabel ?productlabel .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:vendor ?vendorname .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:vendorhomepage ?vendorhomepage . 
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:offerURL ?offerURL .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:price ?price .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:deliveryDays ?deliveryDays .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm-export:validuntil ?validTo } 
WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:product ?productURI .
        ?productURI rdfs:label ?productlabel .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:vendor ?vendorURI .
        ?vendorURI rdfs:label ?vendorname .
        ?vendorURI foaf:homepage ?vendorhomepage .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:offerWebpage ?offerURL .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:price ?price .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:deliveryDays ?deliveryDays .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor$/Offer$> bsbm:validTo ?validTo }