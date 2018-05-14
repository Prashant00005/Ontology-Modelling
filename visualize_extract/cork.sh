#!/bin/bash
ARQ=../lib/apache-jena-3.5.0/bin/arq 
RAPTOR=~/gits/raptor2-2.0.15/utils/rapper
INPUT=../ontologies/kdeOntologyLoaded.ttl
NAME=cork_city

cat << EOF > $NAME.sparql
prefix : <http://www.semanticweb.org/kdeProject/group4/kdeOntology#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>

describe ?s  where {
  ?s a :City ;
     ?p ?o .
  #?o ?p2 ?o2 .
 #?o2 ?p3 ?o3 .

  FILTER(?s = :City_Cork_City)
}
EOF

$ARQ --query $NAME.sparql --data $INPUT > ${NAME}_extract.ttl
$RAPTOR --output dot -i turtle ${NAME}_extract.ttl > ${NAME}_extract.dot
dot -T png ${NAME}_extract.dot -o ${NAME}_extract.png



