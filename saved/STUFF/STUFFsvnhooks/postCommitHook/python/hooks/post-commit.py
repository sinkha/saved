#!/usr/bin/python

"""
This post-commit script looks at the SVN commit details to determine which 
paths in the repository are being modified.  Based on this information,
it then determines which Hudson continuous integration build jobs to trigger so
that each changed project can be rebuilt.
"""

from __future__ import with_statement
import optparse
import os
import os.path
import re
import shutil
import subprocess
import time
import urlparse
import urllib2

def main():
    # Properties file to look in to for the environment values set by configure
    PROPERTIES_FILE = "postCommitHook.properties"
    PROPERTIES_PATH = os.path.join(".", "tools", "post-commit", "lib", PROPERTIES_FILE)

    # Builds an environment context for script execution.
    env = get_environ(PROPERTIES_PATH)
    env = check_args(env)

    # Rotates the log files if necessary
    rotate_logs(env)
  
    # Determines which SVN paths were modified as part of the revision.
    changed_paths = get_svn_changes(env)

    # Determines which build URLs to trigger for the changed SVN paths.
    build_urls = get_urls(env, changed_paths)

    if build_urls:
        # Output timestamp to log for this execution of the post-commit hook.
        write_out_header(env)
        
        if is_a_hudson_commit(env):
            logfile = os.path.join(env['LOGDIR'], env['CLIENT_LOG'])
            
            with open(logfile, 'a') as log:
                log.write("Build trigger skipped (REPO="+env['REPO']+" : REV="+env['REV']+") \n");
        else:
            # Trigger request once for each build URL.
            trigger_url(env, build_urls)


def get_environ(prop_file):
    """
    get_environ(str) -> dict
    
    get_environ examines the system environment and supplements it with the
    properties it reads in from the post-commit hook script properties file.
    See the configure.py script for details on how the properties file is 
    generated.
    """
    env = os.environ

    with open(prop_file) as inf:
        for line in inf:
            line = line.strip("\n")
            if line == "":
                continue
            env_name, env_val = line.split("=", 1)
            env[env_name] = env_val

    return env

    
def check_args(env):
    """
    check_args() -> tup
    
    check_args validates the command line arguments and options.
    """
    usage = "usage: %prog REPO REV"
    
    parser = optparse.OptionParser(usage)

    (options, args) = parser.parse_args()
    
    if len(args) != 2:
        parser.error("Incorrect number of arguments.")

    if not os.path.exists(args[0]):
        parser.error("SVN repository is not a valid directory.")

    if not re.match(r'\d+', args[1]):
        parser.error("SVN revision number is not valid.")

    env["REPO"] = args[0]
    env["REV"] = args[1]
    return env
    

def rotate_logs(env):
    """
    rotate_logs(dict)
    
    rotate_logs checks if the size of the post-commit script's log file exceeds
    a max size and rotates it out if it does.
    """
    logfile = os.path.join(env['LOGDIR'], env['CLIENT_LOG'])
    
    file_size = os.stat(logfile).st_size
    
    if file_size >= int(env['MAX_LOG_FILE_SIZE']):
        archive_file = os.path.join(env['ARCHIVEDIR'], env['CLIENT_LOG'])
        archive_old = "%s_%s" % (archive_file, time.strftime("%Y-%m-%d"))
                    
        shutil.copy(logfile, archive_old)

        # Overwrites the contents of the log file with nothing
        open(logfile, 'w').close()


def get_svn_changes(env):
    """
    get_svn_changes(dict) -> list
    
    get_svn_changes examines the passed in environment dict, interrogates the
    appropriate SVN repository, and returns a list of directories that changed
    with the current revision.
    """

    # svnlook dirs-changed <FULL PATH to SVN REPO> -r <REVISION NUMBER>
    command = ["svnlook", "dirs-changed", env['REPO'], "-r", env['REV']]

    paths = call_process(command)
    return [path for path in paths.split("\n") if path != ""]
    

def get_urls(env, check_paths):
    """
    get_urls(dict, list) -> list
    
    get_urls takes in a list of paths that changed in this revision and 
    returns a list of Hudson build URLs that should be triggered by the
    changes.
    """
    prop_values = set()

    check_paths = [path.rstrip("/") for path in check_paths]

    for repo_path in check_paths:
        for path in get_ancestors(repo_path):
            propval = get_prop_value(env, path)

            if propval:
                prop_values.add(propval)
                break
    
    return list(prop_values)
    

def write_out_header(env):
    """
    write_out_header(dict)
    
    write_out_header writes out a time stamped header to the hook script log.
    """
    logfile = os.path.join(env['LOGDIR'], env['CLIENT_LOG'])

    with open(logfile, 'a') as outf:
        outf.write("\n")
        outf.write("----------------------------------------------------------\n")
        outf.write("date %s\n" % time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))
        outf.write("----------------------------------------------------------\n")


def trigger_url(env, build_urls):
    """
    trigger_url(dict, list)
    
    trigger_url sends a request once for every distinct build in the build URLs list.
    """
    logfile = os.path.join(env['LOGDIR'], env['CLIENT_LOG'])

    for url in build_urls:
        
        params = parse_url_parameters(url)
        
        if('delay' not in params):
        	url += "&delay=%ssec" % env['DEFAULT_DELAY']
        
        if(not(url.startswith('http'))):
            url = env['URL'] + url
        
        with open(logfile, 'a') as log:
            log.write("Calling Build server ("+url+" ... REPO="+env['REPO']+" : REV="+env['REV']+") \n");
            try:
                response = urllib2.urlopen(url)
            except Exception, err:
               log.write("ERROR: " + str(err) + "\n")


def parse_url_parameters(url):
    """
    parse_url_parameters(url) -> dict
    
    parse_url_parameters takes in a URL string and returns a dictionary of all paramters.
    """
    url_parsed = urlparse.urlparse(url)
    
    parts = url_parsed[4].split('&')
    
    if(len(parts) > 1):
        return dict([p.split('=') for p in parts])
    else:
        return {} 


def get_ancestors(repo_path):
    """
    get_ancestors(str) -> list
    
    get_ancestors takes in a SVN path and returns a list of all paths between the
    passed in path and the repository root path.
    """
    check_paths = []

    head, tail = os.path.split(repo_path)

    while head:
        check_paths.append(repo_path)
        head, tail = os.path.split(repo_path)
        repo_path = head

    return check_paths

def is_a_hudson_commit(env):
    """
    is_a_hudson_commit(dict) -> boolean
    
    is_a_hudson_commit returns a boolean True if it was Hudson who commited, False otherwise.
    """
    comment_log = get_comment_log_value(env)
    
    for substr in env['ANTI_TRIGGER_COMMENT'].split(','):
        if substr in comment_log:
            return True
    
    return False

def get_comment_log_value(env):
    """
    get_comment_log_value(dict) -> str
    
    get_comment_log_value returns a comment log
    """
    # svnlook info <Absolute Path to SVN REPOSITORY> -r <REVISION NUMBER>
    command =  ["svnlook", "info", env['REPO'], "-r", env['REV']]
    
    return call_process(command)


def get_prop_value(env, path):
    """
    get_prop_value(dict, str) -> str
 
    get_prop_value takes in a SVN repository path and returns a build URL
    associated to that path or None if one isn't assigned.    
    """
    # svnlook propget <Absolute Path to SVN REPOSITORY> <HUDSON PROPERTY> <PROJECT PATH> -r <REVISION NUMBER>
    command = ["svnlook", "propget", env['REPO'], env['HUDSON_PROPERTY'], path, "-r", env['REV']]

    return call_process(command)


def get_paths(repo_path):
    """
    get_paths(str) -> list
    
    get_paths takes in a SVN path and returns a list of all paths between the
    passed in path and the repository root path.
    """
    check_paths = []

    head, tail = os.path.split(repo_path)

    while head:
        check_paths.append(repo_path)
        head, tail = os.path.split(repo_path)
        repo_path = head

    return check_paths
   

def call_process(command_string):
    """
    call_process(str) -> str or None
    
    call_process invokes a subprocess for the command string and returns the
    call output if any or None.
    """
    proc = subprocess.Popen(command_string, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    
    result = proc.communicate()[0].rstrip("\n")
    
    return result if result else None

    
if __name__ == "__main__":
    main()