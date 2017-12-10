[QueryItem="wellbore"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>

SELECT * {

?wellbore a :Wellbore ;
   :name ?wellborename ; 
   :dateWellboreEntry ?dateWellboreEntry ;
   :dateWellboreCompletion ?dateWellboreCompletion ;
   :wellboreForLicence 
       [  
         a :ProductionLicence ; 
       	 :name ?licencename  
       ] ;
   :drillingOperatorCompany 
       [ 
         a :Company ; 
       	 :name ?companyname 
       ] ;
   :drillingFacility 
       [ 
         a :Facility ; 
       	 :name ?facilityname ;
       	 :facilityType ?facilityType ;
   	 :facilityFunction ?facilityFunction 
       ] 
   .
}
