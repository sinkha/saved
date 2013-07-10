#! /bin/bash

echo Number of args passed: $#
if [ 1 -eq $# ] ; then
	echo Searching pom files for...$1
	echo Searching Releases...
	find ../sonatype-work/nexus/storage/releases/* -name *.pom -type f -print0 | xargs -0 grep -l $1
	
	echo Searching Snapshots...
	find ../sonatype-work/nexus/storage/snapshots/* -name *.pom -type f -print0 | xargs -0 grep -1 $1
else
	echo "Usage: search-cdm-poms.sh <string to search for>"
fi

