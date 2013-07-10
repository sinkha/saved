#!/usr/bin/python

#
# This script is used to identify any components which have multiple snapshot
# versions available. There are several reasons there might be multiple
# snapshot versions: (1) there are multiple development (trunk and bugfix)
# branches being worked on concurrently, (2) a snapshot was abandoned without
# creating a release for that version, (3) ??. If a snapshot was abandoned,
# it will never get cleaned up by Nexus. (Nexus used released versions to 
# identify defunct snapshot versions for clean up). This provides a list of
# the artifacts with multiple snapshot branches. 
#
# A human must go through the generated script file and cull the snapshot 
# versions that should be retained. Then execute the script to remove the
# unnecessary branches.
#
# Usage: search-remove-multiple-snapshots.py <dir> >> snapshot-results.txt
#
# Example usage: python search-remove-multiple-snapshots.py ../sonatype/nexus/storage/snapshots/ > snapshot-results.txt

import os
import sys

def search_for_snapshot(root_dir_path):
        found = []
        for root, dirs, files in os.walk(root_dir_path):
                count = 0
                for dir in dirs:
                        if "SNAPSHOT" in dir:
                                count += 1
                if(count > 1):
                        found.append({'path': root, 'dirs': dirs})
        return found

for found in search_for_snapshot(sys.argv[1]):
    print "\n"
    for dir in found['dirs']:
        if "SNAPSHOT" in dir:
                print "rm -rf "+found['path']+"/"+dir
