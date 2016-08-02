#!/bin/bash

# ./mixerGrepper.sh nMixes


# In the function hardTable (), put at the top of the for cycle the names of the queries you want to analyze
# e.g., for i in 01 03 04 (analyzes queries 01.q, 03.q, and 04.q)

nMixes=$1

init () {
    rm -f *.temp
    rm -rf results
    mkdir results
}

awkMixesTimes () {
    cat statsMixer.txt | grep "mix_time" | awk '{print $4}' > results/mixes_times.stat    
}

averageAndTotalAndStdDev () {
    #By "average" I mean ... well, mean.
    ./averager.sh results/mixes_times.stat > results/aggrMixes_stats.stat
    AVG=`cat results/aggrMixes_stats.stat`
    echo AVG Mix time : $AVG
    cat results/mixes_times.stat | awk '{tot+=$1}END{print tot}' >> results/aggrMixes_stats.stat
    echo \#QMixesPerHour
    TOT=`cat results/mixes_times.stat | awk '{tot+=$1}END{print tot}'` 
    QMPH=$((3600000/(TOT/10)))
    echo $QMPH
    STDEV=`./stdDev.sh $AVG results/mixes_times.stat` #>> results/aggrMixes_stats.stat
    echo $\# StdDev
    echo $STDEV
}

easyTable () {
    #make an awk of all ex times
    cat statsMixer.txt | grep "execution_time" | awk '{print $4}' > results/exTimes.stat
    AVGEX=`./averager.sh results/exTimes.stat`
    cat statsMixer.txt | grep "resultset_traversal_time" | awk '{print $4}' > results/outTimes.stat
    AVGOUT=`./averager.sh results/outTimes.stat`
    cat statsMixer.txt | grep "num_results" | awk '{print $4}' > results/nResults.stat
    AVGNRES=`./averager.sh results/nResults.stat`
    
    echo \# avgEx avgOut avgNRes
    echo $AVGEX $AVGOUT $AVGNRES
}

splitMixes () {
    nMixess=$1
    for((i=0; i<nMixess; i++))
    do
	cat statsMixer.txt | grep "run#${i}" > temp_${i}.temp
    done;
}

grepQueryUnfTime () {
    query=$1
    cat statsMixer.txt | grep "unfolding_time#${query}.q" > temp_unfolding${query}.temp
    cat temp_unfolding${query}.temp | awk '{print $4}' > unfoldingTimes_${query}.stat
    rm temp_unfolding${query}.temp
}

grepQueryRewTime () {
    query=$1
    cat statsMixer.txt | grep "rewriting_time#${query}.q" > temp_rewriting${query}.temp
    cat temp_rewriting${query}.temp | awk '{print $4}' > rewritingTimes_${query}.stat
    rm temp_rewriting${query}.temp
}

grepQueryRespTime () {
    query=$1
    cat statsMixer.txt | grep "execution_time#${query}.q" > temp_execution${query}.temp
    cat temp_execution${query}.temp | awk '{print $4}' > executionTimes_${query}.stat
    
    cat statsMixer.txt | grep "resultset_traversal_time#${query}.q" > temp_traversal${query}.temp
    cat temp_traversal${query}.temp | awk '{print $4}' >> traversalTimes_${query}.stat

    cat executionTimes_${query}.stat | while read line
    do
	cat traversalTimes_${query}.stat | awk '{print '$line'+$1}' > exPlusTraversalTimes_${query}.stat
    done    
}

grepQueryRewSize () {
    query=$1
    cat statsMixer.txt | grep "rewriting_size#${query}.q" | awk '{if(NR=1)print $4}'
}

grepQueryUnfSize () {
    query=$1
    cat statsMixer.txt | grep "unfolding_size#${query}.q" | awk '{if(NR=1)print $4}'
}

hardTable () {
    for i in 01 03 04 
    do
	grepQueryUnfTime $i
	echo \#avg_unfTime $i
	echo `./averager.sh unfoldingTimes_${i}.stat`
	grepQueryRewTime $i
	echo \#avg_rewTime $i
	echo `./averager.sh rewritingTimes_${i}.stat`
	grepQueryRespTime $i
	echo \#avg_respTime $i
	echo `./averager.sh exPlusTraversalTimes_${i}.stat`
	echo \#rew_size $i
	echo grepQueryRewSize $i 
    done
}

init
awkMixesTimes
averageAndTotalAndStdDev
easyTable
#hardTable

#cat stats.txt | grep "GLOBAL" > global_stats.tmp
