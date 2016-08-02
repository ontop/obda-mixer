#!/bin/bash

rm -rf *.temp

method () {
    query=$1
    
    cat statsMixer.txt | grep "unfolding_time#${query}.q" | awk '{print $4}' > unfoldingTimes.temp
    cat statsMixer.txt | grep "rewriting_time#${query}.q" | awk '{print $4}' > rewritingTimes.temp
    cat statsMixer.txt | grep "execution_time#${query}.q" | awk '{print $4}' > executionTimes.temp
    
    paste unfoldingTimes.temp rewritingTimes.temp > rwUnf.temp
    paste rwUnf.temp executionTimes.temp > allTimes.temp
    
    cat allTimes.temp | awk '{print $1+$2+$3}' > totSums.temp
    
    ./averager.sh totSums.temp
}

for i in 01 02 03 04 05
do
    echo $i `method $i`
done