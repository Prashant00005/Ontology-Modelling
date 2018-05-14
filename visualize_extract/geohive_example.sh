#!/bin/bash
ARQ=../lib/apache-jena-3.5.0/bin/arq 
RAPTOR=~/gits/raptor2-2.0.15/utils/rapper
INPUT=../data/data.geohive.ie/county_100m.ttl
NAME=geohive_example

cat << EOF > $NAME.sparql
prefix : <http://www.semanticweb.org/kdeProject/group4/kdeOntology#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>

describe ?s  where {
  ?s ?p ?o .
  #?o ?p2 ?o2 .
 #?o2 ?p3 ?o3 .

  FILTER(?s = <http://data.geohive.ie/resource/county/2AE19629144813A3E055000000000001>)
}LIMIT 2
EOF

$ARQ --query $NAME.sparql --data $INPUT > ${NAME}_extract.ttl
sed -i "s/\"/'/g" ${NAME}_extract.ttl # fix quotes
sed -i "s/POLYGON ([^\)]*)/POLYGON(\.\.\./g" ${NAME}_extract.ttl # fix polygon
sed -i "s/2AE19629144813A3E055000000000001/2A...001/g" ${NAME}_extract.ttl # fix polygon
sed -i "1s/^/@prefix rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> .\n/g" ${NAME}_extract.ttl # add rdf
sed -i "1s/^/@prefix geo: <http:\/\/www.opengis.net\/ont\/geosparql#> .\n/g" ${NAME}_extract.ttl # add rdf
$RAPTOR --output dot -i turtle ${NAME}_extract.ttl > ${NAME}_extract.dot
dot -T png ${NAME}_extract.dot -o ${NAME}_extract.png



