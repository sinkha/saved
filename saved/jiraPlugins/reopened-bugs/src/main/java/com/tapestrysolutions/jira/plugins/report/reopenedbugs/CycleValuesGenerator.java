package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections.map.ListOrderedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

public class CycleValuesGenerator implements ValuesGenerator {

	public Map<String, String> getValues(Map userParams) {
		//String projectId = (String) userParams.get("projectId");
		//final Long pid = new Long(projectId);
		Map<String, String> projectMap = new ListOrderedMap();
		
		Map<String, String> versionMap = new HashMap<String, String>();

		
		//Collection<Version> allVersions = ComponentManager.getInstance().getVersionManager().getVersions(pid);
		Collection<Version> allVersions = ComponentManager.getInstance().getVersionManager().getAllVersions();

		Collection<String>versionNames = new TreeSet<String>(Collator.getInstance());
		for (Version version : allVersions) {
			versionNames.add(version.getName());
		}
		
		for (String versionName : versionNames) {

			//System.out.println("versionId: " + version.getId().toString() + " versionName: " + version.getName());
			//versionMap.put(version.getId().toString(), version.getName());
			projectMap.put(versionName, versionName);			
		}

		return projectMap;

	}

}

