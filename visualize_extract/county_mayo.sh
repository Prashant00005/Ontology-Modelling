#!/bin/bash
ARQ=../lib/apache-jena-3.5.0/bin/arq
RAPTOR=~/gits/raptor2-2.0.15/utils/rapper
INPUT=../ontologies/kdeOntologyLoaded.ttl
NAME=county_mayo

cat << EOF > $NAME.sparql
prefix : <http://www.semanticweb.org/kdeProject/group4/kdeOntology#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>

describe ?s ?area ?nat where {
  ?s a :County ;
     ?p ?o  ;
     rdfs:label ?label ;
     :hasArea ?area .
  ?nat :hasCounty ?s .
  #?o ?p2 ?o2 .
 #?o2 ?p3 ?o3 .

  FILTER(?label = "Mayo")
}LIMIT 2
EOF

$ARQ --query $NAME.sparql --data $INPUT > ${NAME}_extract.ttl
sed -i "s/AE.*000000000001/AE/g" ${NAME}_extract.ttl
$RAPTOR --output dot -i turtle ${NAME}_extract.ttl > ${NAME}_extract.dot
dot -T png -K dot ${NAME}_extract.dot -o ${NAME}_extract.png



