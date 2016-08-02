#!/bin/bash

#./stdDev.sh avg valuesFile

AVG=$1
valuesFile=$2
cat $valuesFile | awk 'BEGIN{cnt=0;squaresSum=0;}{squaresSum+=($1-'$AVG')*($1-'$AVG'); cnt++;}END{print sqrt(squaresSum / cnt)}'
