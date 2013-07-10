package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.ProjectActionSupport;

public class ReopenedBugsReport extends AbstractReport {
	private final String REOPENED_COUNT_CFNAME = "Re-Opened Count";
	private long MIN_REOPENED_COUNT = 2;

		
	@Override
	public void validate(ProjectActionSupport action, Map reqParams) {

		// validate project
		Object projObj = reqParams.get("projectId");
		if (projObj instanceof String[]) {
			String[] projects = (String[])projObj;
			
			for (String projectName : projects) {
				validateProject(projectName);
			}
		}
		else if (projObj instanceof String) {
			validateProject((String) reqParams.get("projectId"));
		}
		else if (projObj == null) {
			action.addError("projectId", "Please select at least one project.");
		}
		
		//validate project version - version valid if valid for at least one project
		Object versionObj = reqParams.get("versionId");
		if (versionObj instanceof String[] && projObj instanceof String[]) {
			String[] versions = (String[])versionObj;
			String[] projects = (String[])projObj;
			
			for (String versionName : versions) {
				if (!validateVersionForProjects(versionName, projects)) {
					action.addError("versionId", "Invalid version selected");
				}
			}
		}
		else if (versionObj instanceof String[] && projObj instanceof String) {
			String[] versions = (String[])versionObj;
			
			for (String versionName : versions) {
				if (!validateProjectVersion((String)projObj,versionName)) {
					action.addError("versionId", "Invalid version selected");
				}
			}			
		}
		else if (versionObj instanceof String && projObj instanceof String[]) {
			if (!validateVersionForProjects((String)versionObj, (String[])projObj)) {
				action.addError("versionId", "Invalid version selected");
			}
		}
		else if (versionObj instanceof String && projObj instanceof String) {
			if (!validateProjectVersion((String)projObj, (String)versionObj)) {
				action.addError("versionId", "Invalid version selected");
			}
		}
				
		// validate user for project - user valid if valid for at least one project
		final String personId = (String) reqParams.get("personId");
		if (personId != null && personId.trim().length() > 0) {
			if (projObj instanceof String[]) {
				String[] projects = (String[])projObj;
				
				if (!validateUserForProjects(personId, projects)) {
					action.addError("personId", "Invalid user selected");
				}
			}
			else if (projObj instanceof String) {
				if (!validateUserForProject(personId, (String) reqParams.get("projectId"))) {
					action.addError("personId", "Invalid user selected");
				}
			}			
		}
								
		super.validate(action, reqParams);
	}

	
	@Override
	public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception {
		final Map<String, Object> velocityParams = getVelocityParams(action, reqParams);
		return descriptor.getHtml("view", velocityParams);
	}
	
	@Override
	public boolean isExcelViewSupported() {
		return true;
	}
	
	@Override
	public String generateReportExcel(ProjectActionSupport action, Map reqParams) throws Exception {
		final Map<String, Object> velocityParams = getVelocityParams(action, reqParams);
		return descriptor.getHtml("excel", velocityParams);
	}
	
	
	public FilteredBaseIssuesResult getIssuesFromProject(ReopenedBugsSearch search, String projectId, String versionId, String userName) {
		User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
		Collection<Issue> allIssues = search.getIssuesFromProject(projectId, versionId, user, userName);
		
		try {
		
			return search.getFilteredIssues(allIssues);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean showReport() {
		User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
		return ComponentManager.getInstance().getUserUtil().getAdministrators().contains(user);
	}
	
	private boolean validateUserForProjects(String userId, String[] projects) {
		for (String projectName : projects) {
			if (validateUserForProject(userId, projectName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean validateVersionsForProject(String[] versions, String projectName) {
		for (String versionName : versions) {
			if (!validateProjectVersion(projectName, versionName)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean validateUserForProject(String userId, String projectName) {
		UserManager userManager = ComponentManager.getComponentInstanceOfType(UserManager.class);
		User user = userManager.getUserObject(userId);
		
		Project project = ComponentManager.getInstance().getProjectManager().getProjectObjByName(projectName);
		
		Collection<ProjectRole> projectRoles = ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class).getProjectRoles(user, project);
		if (projectRoles == null || projectRoles.isEmpty()) {
			return false;
		}			
		
		return true;
	}
	
	private boolean validateProject(String name) {
		Project project = ComponentManager.getInstance().getProjectManager().getProjectObjByName(name);
		if (project == null) {
			return false;
		}
		
		return true;
	}
	
	private boolean validateVersionForProjects(String versionName, String[] projects) {
		
		for (String projectName : projects) {
			if (validateProjectVersion(projectName, versionName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean validateProjectVersion(String projectName, String versionName) {
		Project project = ComponentManager.getInstance().getProjectManager().getProjectObjByName(projectName);
		long pid = project.getId();
		
		Version version = ComponentManager.getInstance().getVersionManager().getVersion(pid, versionName);
		if (version == null) {
			return false;
		}
		
		return true;		
	}
	
	private String createStringFromArray(String[] values) {
		StringBuilder sb = new StringBuilder();
		
		for (String value : values) {
			sb.append(value + ",");
		}

		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
	private Map<String, Object> getVelocityParams(ProjectActionSupport action, Map reqParams) throws SearchException {
		Object projObj = reqParams.get("projectId");
		
		String projectName = "";
		if (projObj instanceof String[]) {
			projectName = createStringFromArray((String[])projObj);
		}
		else if (projObj instanceof String) {
			projectName = (String)projObj;
		}
		
		Object versionObj = reqParams.get("versionId");
		String versionName = "";
		if (versionObj instanceof String[]) {
			versionName = createStringFromArray((String[])versionObj);
		}
		else {
			versionName = (String)versionObj;
		}

		String userName = (String)reqParams.get("personId");
		
		ReopenedBugsSummary summary = createSummary(projectName, versionName, userName);
		
		final Map<String, Object> velocityParams = new HashMap<String, Object>();
		
		velocityParams.put("report", this);
		velocityParams.put("action", action);
		velocityParams.put("issues", summary.getRows());
		velocityParams.put("summary", summary);
		velocityParams.put("project", projectName);
		velocityParams.put("version", versionName);

		return velocityParams;
		
	}
		
    private void applyJqlStrings(ReopenedBugsSummary summary, String searcher, String projectId, String versionId, String userId) {
    	String baseFixed;
    	String baseReopened;
    	
    	
    	if (userId != null && userId.trim().length() > 0) {
        	baseFixed = "issue in " + searcher + "(\"" + projectId + "\", \"" + versionId + "\", \"" + userId + "\"  )";
        	baseReopened = "issue in " + searcher + "(\"" + projectId + "\", \"" + versionId + "\", \"" + userId + "\")";
    	}
    	else {
        	baseFixed = "issue in " + searcher + "(\"" + projectId + "\", \"" + versionId + "\"  )";
        	baseReopened = "issue in " + searcher + "(\"" + projectId + "\", \"" + versionId + "\")";    		
    	}
    	
    	try {
    		summary.modifyBugsFixedJql(URLEncoder.encode(baseFixed, "UTF-8"));
    		summary.modifyBugsReopenedJql(URLEncoder.encode(baseReopened, "UTF-8"));
    	}catch(Exception e) {}
    	
    	for (ReopenedBugsRow row : summary.getRows()) {
    		String reopened = baseReopened + " and \"" + REOPENED_COUNT_CFNAME + "\" >= \"" + MIN_REOPENED_COUNT + "\"" + " and assignee= \"" + row.getName() + "\"";
    		String fixed = baseFixed + " and assignee= \"" + row.getName() + "\"";
    		
    		
    		try {
    			row.modifyBugsFixedJql(URLEncoder.encode(fixed, "UTF-8"));
    			row.modifyBugsReopenedJql(URLEncoder.encode(reopened, "UTF-8"));
    		}catch(Exception e) {}
    		
    	}
    }


    private ReopenedBugsSummary createSummary(String projectId, String versionId, String userName) {
    	ReopenedBugsSearch search = new ReopenedBugsSearch();
		ReopenedBugsSummary summary = new ReopenedBugsSummary();
		FilteredBaseIssuesResult result = getIssuesFromProject(search, projectId, versionId, userName);
		
		
		Collection<Issue> bugsFixed = result.getBugsFixed();
		Collection<Issue> bugsReopened = result.getBugsReopened();
		
		
		for (Issue issue : bugsFixed) {
			User user = issue.getAssigneeUser();

			String name = "Not Assigned";

			if (user != null) {
				name = user.getDisplayName();
			}
						
			ReopenedBugsRow row = summary.getOrCreateRow(name);
			row.incBugsFixed();
			row.modifyPercentageReopened();
		}
		
		for (Issue issue : bugsReopened) {
			String name = issue.getAssigneeUser().getDisplayName();
			ReopenedBugsRow row = summary.getOrCreateRow(name);
			row.incBugsReopened();
			row.modifyPercentageReopened();
		}
		
		summary.modifyTotalFixed(bugsFixed.size());
		summary.modifyTotalReopened(bugsReopened.size());
		summary.modifyPercentageReopened();
		
		if (versionId == null) {
			versionId = "";
		}
		
		if (userName == null) {
			userName = "";
		}
		
		applyJqlStrings(summary, search.getFunctionName(), projectId, versionId, userName);
		
		
		return summary;
    	
    }    	
	
	public static class ReopenedBugsSummary {
		private ArrayList<ReopenedBugsRow> rows;
		private long totalFixed;
		private long totalReopened;
		private long percentageReopened;
		private String bugsFixedJql;
		private String bugsReopenedJql;
		
		ReopenedBugsSummary() {
			this.rows = new ArrayList<ReopenedBugsRow>();
			this.totalFixed = 0;
			this.totalReopened = 0;
			this.percentageReopened = 0;
			
		}
		
		public ArrayList<ReopenedBugsRow> getRows() {
			Collections.sort(rows, ReopenedBugsRow.ReopenedBugRowComparator);
			return rows;
		}
		
		public long getTotalFixed() {
			return this.totalFixed;
		}
		
		public long getTotalReopened() {
			return this.totalReopened;
		}
		
		public long getPercentageReopened() {
			return this.percentageReopened;
		}
		
    	public void modifyBugsFixedJql(String value) {
    		bugsFixedJql = value;
    	}
    	
    	public void modifyBugsReopenedJql(String value) {
    		bugsReopenedJql = value;
    	}
    	
		public void modifyTotalFixed(long value) {
			this.totalFixed = value;
		}
		
		public void modifyTotalReopened(long value) {
			this.totalReopened = value;
		}
		
		public void modifyPercentageReopened() {
			if (this.totalFixed > 0) {
				
				float percent = ((float)this.totalReopened/(float)this.totalFixed) * 100;
				this.percentageReopened = (long) Math.round(percent);
			}
		}
		
		
		public String getBugsFixedJql() {
			return this.bugsFixedJql;
		}
		
		public String getBugsReopenedJql() {
			return this.bugsReopenedJql;
		}
		
    	public ReopenedBugsRow getOrCreateRow(String rowName) {
    		ReopenedBugsRow temp = new ReopenedBugsRow(rowName);
        	int index = rows.indexOf(temp);
        	if (index == -1) {
        		rows.add(temp);
        	}
        	else {
        		temp = rows.get(index);
        	}
        	return temp;
    	}
		
	}
	
	public static class ReopenedBugsRow implements Comparable<ReopenedBugsRow>{
		private String name;
		private String bugsFixedJql;
		private String bugsReopenedJql;
		private long bugsFixed;
		private long bugsReopened;
		private long percentageReopened;
		
		
		ReopenedBugsRow(String name) {
			this.name = name;
			bugsFixed = 0;
			bugsReopened = 0;
			percentageReopened = 0;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getBugsFixedJql () {
			return this.bugsFixedJql;
		}
		
		public String getBugsReopenedJql() {
			return this.bugsReopenedJql;
		}
		
		public long getBugsFixed() {
			return this.bugsFixed;
		}
		
		public long getBugsReopened() {
			return this.bugsReopened;
		}
		
		public long getPercentageReopened() {
			return this.percentageReopened;
		}
		
		public void incBugsFixed() {
			this.bugsFixed++;
		}
		
		public void incBugsReopened() {
			this.bugsReopened++;
		}
		
		public void modifyPercentageReopened() {			
			if (this.bugsFixed > 0) {
				float percent = ((float)this.bugsReopened/(float)this.bugsFixed) * 100;
				this.percentageReopened = (long) Math.round(percent);
				
			}
		}
		
		public void modifyBugsFixedJql(String jql) {
			this.bugsFixedJql = jql;
		}
		
		public void modifyBugsReopenedJql(String jql) {
			this.bugsReopenedJql = jql;
		}

		@Override
		public int compareTo(ReopenedBugsRow o) {
			// TODO Auto-generated method stub
			return (int) ((this.getBugsFixed() + this.getBugsReopened()) - (o.getBugsFixed() + o.getBugsReopened()));
		}
		
    	public boolean equals(Object o) {
    		if (o instanceof ReopenedBugsRow) {
    			return ((ReopenedBugsRow) o).getName().equals(this.getName());
    		}
    		return false;
    	}
    	
    	public static Comparator<ReopenedBugsRow> ReopenedBugRowComparator = new Comparator<ReopenedBugsRow>() {
    		public int compare(ReopenedBugsRow row1, ReopenedBugsRow row2) {
    			String rowName1 = row1.getName().toUpperCase();
    			String rowName2 = row2.getName().toUpperCase();
    			
    			return rowName1.compareTo(rowName2);
    			
    		}
    	};
	}
}
