#!/bin/bash

#Evaluates the average of all the rows in the file

cat $1 | awk 'FNR==1 {nf=NF}
{
if(NR != 0){
 for(i=1; i<=NF; i++)
   arr[i]+=$i
  fnr=FNR
}
}
END {
  for( i=1; i<=nf; i++)
   printf("%.0f%s", arr[i] / fnr, (i==nf) ? "\n" : FS)
}'