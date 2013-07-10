package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;

/**
 * This class generates map for cascadingselect component type. map contains mapping between projects and their version.
 * Mapping contains also unspecified version "---" for every project to allow "unspecified version".
 *
 */
public class ProjectVersionValuesGenerator implements ValuesGenerator {
	 
	    public Map getValues(Map arg0) {
	        VersionManager versionManager = ComponentManager.getInstance().getVersionManager();
	        JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
	 
	        Collection<Project> projects = ComponentAccessor.getPermissionManager().getProjectObjects(Permissions.BROWSE,
	                ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
	 
	        Map<String, ValueClassHolder> allVersions = new ListOrderedMap();
	        for (Iterator<Project> iterator = projects.iterator(); iterator.hasNext();) {
	            Project project = iterator.next();
	            Collection<Version> versions = versionManager.getVersions(project.getId());

	            for (Iterator<Version> versionIt = versions.iterator(); versionIt.hasNext();) {
	                Version version = versionIt.next();
	                if (!version.isArchived()) {
	                	allVersions.put(version.getId().toString(), new ValueClassHolder(version.getName(), project.getId().toString()));
	                }
	            }
	        }
	        
	        return allVersions;
	    }	       


    private static class ValueClassHolder {
        private String value;
        private String className;

        public ValueClassHolder(String value, String className) {
            this.value = value;
            this.className = className;
        }

        public String getValue() {
            return value;
        }

        public String getClassName() {
            return className;
        }

        public String toString() {
            return value;
        }
    }
}
