#!/usr/bin/python

#
# Displays the path to nexus artifacts for which there are multiple
# snapshot versions available.
#
# Usage: search-multiple-snapshots.py <dir>
#
#

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
        print found['path']
        for dir in found['dirs']:
                if "SNAPSHOT" in dir:
                        print "\t"+dir