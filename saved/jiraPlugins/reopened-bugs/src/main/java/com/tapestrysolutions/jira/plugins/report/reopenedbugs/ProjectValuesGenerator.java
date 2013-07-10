package com.tapestrysolutions.jira.plugins.report.reopenedbugs;


import java.text.Collator;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections.map.ListOrderedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.project.Project;

public class ProjectValuesGenerator implements ValuesGenerator {
	
	public Map<String, String> getValues(Map userParams) {
		//Map<String, String> projectMap = new HashMap<String, String>();
		//Map map = new ListOrderedMap();
		Map<String, String> projectMap = new ListOrderedMap();
		Map<String, Project> projects = new HashMap<String,Project>();
		Collection<String>projectNames = new TreeSet<String>(Collator.getInstance());
		
		List<Project> allProjects = ComponentManager.getInstance().getProjectManager().getProjectObjects();
		
		for (Project project : allProjects) {
			projectNames.add(project.getName());
			projects.put(project.getName(), project);
		}
		
		for (String projectName : projectNames) {

			//System.out.println("projectID: " + project.getId() + ":" + project.getName());
			Project project = projects.get(projectName);
			projectMap.put(project.getName(), project.getName());
		}

		return projectMap;

	}	

}
