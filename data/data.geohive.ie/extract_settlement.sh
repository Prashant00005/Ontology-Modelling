#!/bin/bash

# Base Script File (extract_settlement.sh)
# Created: sam. 02 déc. 2017 17:19:14 GMT
# Version: 1.0
#
# This Bash script was developped by Cory.
#
# (c) Cory <sgryco@gmail.com>

fd=fulldump.ttl

#get settlements
grep --no-group-separator "^<http://data.geohive.ie/resource/census2011-settlements/" fulldump.ttl -A4 > settlements.ttl
grep --no-group-separator "_:b" settlements.ttl  > geom.txt
sed -i "s/[; \.]//g" geom.txt
sed -i "s/^/\^/g" geom.txt
sed -i "s/$/ /g" geom.txt

grep --no-group-separator -f geom.txt -A3 fulldump.ttl >> settlements.ttl

#for i in `cat geom.txt`; do
  #echo $i
  #grep --no-group-separator "^$i " -A3 fulldump.ttl >> settlements.ttl
#done


