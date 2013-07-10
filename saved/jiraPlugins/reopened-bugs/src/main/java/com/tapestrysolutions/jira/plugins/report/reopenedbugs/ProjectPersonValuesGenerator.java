package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;

import com.atlassian.crowd.embedded.api.User;

public class ProjectPersonValuesGenerator implements ValuesGenerator {

	@Override
	public Map getValues(Map arg0) {
		//System.out.println("ProjectPersonValuesGenerator: getValues()");
		//String projectId = (String) arg0.get("projectId");
		//final Long pid = new Long(projectId);
		
		Map<String, ValueClassHolder> usersMap = new HashMap<String, ValueClassHolder>();

		
		PermissionManager permissionMananger = ComponentManager.getInstance().getPermissionManager();
		ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);		
		
		JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
		

		Collection<Project> projects = permissionMananger.getProjectObjects(Permissions.BROWSE, 
				ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
		
		
		Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles();

		for (Project project : projects) {
			for (ProjectRole projectRole : projectRoles) {
				ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole,project);
				Set<com.opensymphony.user.User> users = projectRoleActors.getUsers();				
				for (User user : users) {
					if (user.isActive()) {
						usersMap.put(user.getName(), new ValueClassHolder(user.getDisplayName(), project.getId().toString()));
					}
				}
				
			}									
		}


		return usersMap;		
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
