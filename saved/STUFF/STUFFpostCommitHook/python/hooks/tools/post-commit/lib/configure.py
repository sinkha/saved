#!/usr/bin/python

"""
configure.py is a script designed to perform all of the setup tasks required
to make the post-commit hook scripts and server application work properly.  It
functions by prompting the user for certain properties and inspecting the 
system environment to establish the values that should be written out to a
properties file.  These properties in turn are used by the post-commit.py.
"""
###############################################################################
#
# Filename: configure.py
# Author:   John Siegrist
# Date:     11/03/09
# Revision: 1.0
#
# Updates:
#	Author:		Evan Mjelde
#	Date: 		6/24/10
#	Revision:	1.1
#
#   Author:     Karla Jacobsen 
#   Date:       10/18/12
#   Revision:   1.2
#
###############################################################################

from __future__ import with_statement
import os
import os.path
import re
import stat
import subprocess
import urlparse

def main():
    print "This script will configure the SVN post-commit hook scripts.\n"
    
    properties = get_user_responses()
    properties = configure_environment(properties)
    properties = configure_logs(properties)

    make_launcher(properties)
    
    set_permissions(properties)

    write_out_properties(properties)

    print "Configuration is now complete.\n"
    

def get_user_responses():
    """
    get_user_responses() -> dict
    
    get_user_responses queries the user for values that would otherwise have
    been directly edited by hand in the hook script properties file.  These
    values become part of the environment "properties" dict and are later
    written out to the properties file.
    """
    props = { 
        "DEFAULT_DELAY": 300,
        "ANTI_TRIGGER_COMMENT": "[HUDSON,JENKINS]",
    }
    
    accepted = False
    err_msg = "Invalid: Delay time must be a number."

    while( not accepted ):
        instr = raw_input("How long should the default delay be in seconds? (default: 300): ")
        
        if not instr:
            accepted = True
        elif re.match(r'\d+', instr):
            props['DEFAULT_DELAY'] = int(instr)
            accepted = True
        else:
            print err_msg
    
    accepted = False
    err_msg = "Invalid input."
    
    while( not accepted ):
       instr = raw_input("What sub-string should be in a comment log to skip the build-trigger? (multiple values may be added separated by a comma ',' .. default: [HUDSON,JENKINS]): ")
       
       if not instr:
           accepted = True
       elif len(instr) > 0:
           props['ANTI_TRIGGER_COMMENT'] = instr
           accepted = True
       else:
           print err_msg
    
    return props
    

def configure_environment(props):
    """
    configure_environment(dict) -> dict
    
    configure_environment adds additional entries to the environment 
    "properties" dict that are needed by the hook script and hook server.
    """
    props["HOOK_TOOLS_LIB"] = os.path.abspath(".")

    props["HUDSON_PROPERTY"] = "build"

    props['MAX_LOG_FILE_SIZE'] = 1000 * 1024
    
    props['URL'] = "https://build.devnet.local/hudson"
        
    if "PATH" in os.environ:
        props["PATH"] = os.environ["PATH"]
    else:
        raise Exception("PATH environment variable not found")
        
    return props
    

def configure_logs(props):
    """
    configure_logs(dict) -> dict
    
    configure logs creates the initial log directories and log files and 
    appends the environment "properties" dict logfile and path information.    
    """
    base_log_path = os.path.join(props['HOOK_TOOLS_LIB'], "..", "..", "..", "logs", "post-commit")

    # Path to the hook script client and server log files
    props['LOGDIR'] = os.path.abspath(base_log_path)
    props['ARCHIVEDIR'] = os.path.join(props['LOGDIR'], "archived-logs")

    # Client log file
    props['CLIENT_LOG'] = "start-post-commit.log"
    client_log = os.path.join(props['LOGDIR'], props['CLIENT_LOG'])

    for path in [props['LOGDIR'], props['ARCHIVEDIR']]:
        if not os.path.exists(path):
            print "Creating log directory %s." % (path,)
            os.makedirs(path)
        
    # "touches" empty log files
    for log in [client_log]:
        if not os.path.exists(log):
            print "Creating log file at %s." % (log, )
            open(log, 'w').close()

    return props


def make_launcher(props):
    """
    make_launcher(dict)
    
    make_launcher generates a platform-appropriate launcher for the 
    post-commit hook script.  
    """
    python_dir = get_python_dir()
    hook_dir = os.path.normpath(props['HOOK_TOOLS_LIB'] + "/../../..")

    if os.name == "nt":
        script_file = "post-commit.bat"
        script = "%s post-commit.py %%1 %%2" % python_dir
    elif os.name == 'posix':
        script_file = "post-commit"
        script = "#!%s\ncd %s\n%s post-commit.py $1 $2" % (get_sh_path(), hook_dir, python_dir)

    script_path = os.path.join(hook_dir, script_file)

    with open(script_path, 'w') as outf:
        outf.write(script)
        

def set_permissions(props):
    """
    set_permissions(dict)
    
    set_permissions sets the access permissions on the post-commit scripts 
    and respective log files.
    """
    post_commit_path = os.path.normpath(props['HOOK_TOOLS_LIB'] + "/../../..")
    
    launch_file = "post-commit.bat" if os.name == "nt" else "post-commit"
    launcher = os.path.join(post_commit_path, launch_file)

    client_log = os.path.join(props['LOGDIR'], props['CLIENT_LOG'])

    for path in [client_log, launcher]:
        # equivalent to chmod 755
        # Setting owner permissions to read, write, execute
        os.chmod(path, stat.S_IRUSR|stat.S_IWUSR|stat.S_IXUSR)

        # Setting group permissions to read, execute
        os.chmod(path, stat.S_IRGRP|stat.S_IXGRP)

        # Setting other permissions to read, execute
        os.chmod(path, stat.S_IROTH|stat.S_IXOTH)


def write_out_properties(props):
    """
    write_out_properties(dict)
    
    write_out_properties generates a properties file for the post-commit hook
    scripts and post-commit server.  
    """

    # Hook script properties file name
    file_name = "postCommitHook.properties"
    
    file_path = props['HOOK_TOOLS_LIB']
    
    # Path where the hook script is expected on the system.
    # os.path.join is needed to ensure system-independent path separators
    prop_path = os.path.join(file_path, file_name)

    if not os.path.exists(file_path):
        print "\nCreating properties file directory.\n"
        os.makedirs(file_path)

    print "\nWriting out properties file to %s.\n" % (prop_path,)
    
    with open(prop_path, 'w') as outf:
        for propname in sorted(props):
            outf.write("%s=%s\n" % (propname, props[propname]))


def get_python_dir():
    """
    get_python_dir() -> str
    
    get_python_dir inspects the system path and finds the entry for the python
    interpreter, raising an exception if it isn't found.
    """
    if os.name == 'nt':
        try:
            pyexec = os.environ['PYTHON'] + "\\python.exe"
        except KeyError:
            pyexec = ''
    elif os.name == 'posix':
        command = " ".join(["which", "python"])
        subproc = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        pyexec = subproc.communicate()[0].rstrip("\n")
    else:
        raise Exception("Script running on an OS it wasn't designed for.")
    
    if(pyexec == ''):
        raise Exception("Python not found on the system path.")
    else:
        return pyexec


def get_sh_path():
    """
    get_sh_path() -> str
    
    get_sh_path queries the system to find the path to sh so that it can create
    a system-dependent launcher script on Linux machines.
    """
    command = " ".join(["which", "sh"])

    subproc = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    return subproc.communicate()[0].rstrip("\n")
    
if __name__ == "__main__":
    main()