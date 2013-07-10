package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
//import com.opensymphony.user.User;
import com.atlassian.crowd.embedded.api.User;


public class ReopenedBugsSearch implements JqlFunction {	
	private final int MIN_NUMBER_OF_ARGS = 2;
	private final String ISSUE_TYPE="Bug";
	private final String STATUS_TYPE="Closed";
	private final String REOPENED_COUNT_CFNAME = "Re-Opened Count";
	private long MIN_REOPENED_COUNT = 2;

	protected JqlFunctionModuleDescriptor descriptor;
	
	protected final SearchRequestService searchRequestService;
	protected final ProjectManager projectManager;
	protected final ConstantsManager constantsManager;
	protected final SearchProvider searchProvider;
	protected final CustomFieldManager customFieldManager;
	protected final VersionManager versionManager;
	
	
	public ReopenedBugsSearch() {
		ComponentManager cm = ComponentManager.getInstance();
		
		searchRequestService = cm.getSearchRequestService();
		projectManager = cm.getProjectManager();
		versionManager = cm.getVersionManager();
		constantsManager = cm.getConstantsManager();
		searchProvider = cm.getSearchProvider();
		customFieldManager = cm.getCustomFieldManager();
		
	}
	@Override
	public JiraDataType getDataType() {
		// TODO Auto-generated method stub
		return JiraDataTypes.ISSUE;
	}

	@Override
	public String getFunctionName() {
		// TODO Auto-generated method stub
		return "reopenedBugsSearch";
	}

	@Override
	public int getMinimumNumberOfExpectedArguments() {
		// TODO Auto-generated method stub
		return MIN_NUMBER_OF_ARGS;
	}

	@Override
	public List<QueryLiteral> getValues(QueryCreationContext queryCreationContext,
			FunctionOperand operand, TerminalClause terminalClause) {
		
		User user = queryCreationContext.getUser();
		List<String> arguments = operand.getArgs();
		
		if (arguments.size() < MIN_NUMBER_OF_ARGS) {
			return new ArrayList<QueryLiteral>();
		}

		String project = arguments.get(0);
		String version = arguments.get(1);
		
		String userName = null;
		if (arguments.size() == 3) {
			userName = arguments.get(2);
		}

		Collection<Issue> issues = getIssuesFromProject(project, version, user, userName);
		
		final List<QueryLiteral> literals = new ArrayList<QueryLiteral>(issues.size());
		for (Issue issue : issues) {
			literals.add(new QueryLiteral(operand, issue.getId()));
		}
		
		return literals;	
	}

	@Override
	public void init(JqlFunctionModuleDescriptor descriptor) {
		this.descriptor = descriptor;

	}

	@Override
	public boolean isList() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public MessageSet validate(com.opensymphony.user.User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		// TODO Auto-generated method stub

		MessageSet messages = new MessageSetImpl();
        List<String> arguments = operand.getArgs();

        //Make sure we have the correct number of arguments.
        if (arguments.size() < MIN_NUMBER_OF_ARGS)
        {
            messages.addErrorMessage("Not enough arguments provided");
            return messages;
        }
        
        if (!validateProject(arguments.get(0), searcher)) {
        	if (isProject()) {
        		messages.addErrorMessage("Unable to find project");
        	}
        }
        
        if (arguments.get(1) != null && arguments.get(1).equals("")) {
	        if (!validateVersion(arguments.get(1), arguments.get(0), searcher)) {
	        	messages.addErrorMessage("Unable to find version");
	        }
        }

        return messages;		
	}
	
	public List<Issue> getIssuesFromProject(String projectId, String versionId, User user, String userName) {		
		Query query = getQuery(projectId, versionId, userName);
		
		try {
			return searchProvider.search(query, user, PagerFilter.getUnlimitedFilter()).getIssues();
		}
		catch (Exception e) {
			
		}
		
		return new ArrayList<Issue>();
	}
	
	public FilteredBaseIssuesResult getFilteredIssues(Collection<Issue> issues) {
		FilteredBaseIssuesResult result;
		
		ArrayList<Issue> bugsFixed = new ArrayList<Issue>();
		ArrayList<Issue> bugsReopened = new ArrayList<Issue>();
		
		CustomField cf = customFieldManager.getCustomFieldObjectByName(REOPENED_COUNT_CFNAME);
		
		for (Issue issue : issues) {
			
			if (cf != null) {
				Object cfValue = issue.getCustomFieldValue(cf);
				
				if (cfValue != null) {
					Double reopenedCount = ((Double)cfValue).doubleValue();
					if ((double)reopenedCount >= MIN_REOPENED_COUNT) {
						bugsReopened.add(issue);
					}					
				}
			}
			
			bugsFixed.add(issue);									
					
		}
		
		result = new FilteredBaseIssuesResult(bugsFixed, bugsReopened);
		return result;
	}
	
	protected Query getQuery(String projectId, String versionId, String userName) {
		
		JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
		
		
		Collection<String>projects = buildStringCollection(projectId);
		Collection<String>versions = buildStringCollection(versionId);
		
		JqlClauseBuilder jqlClauseBuilder = builder.where();
		
		boolean addOr = false;
		
		jqlClauseBuilder = jqlClauseBuilder.sub();
		for (String projectName : projects) {
			Collection<String> validVersions = findValidVersionsForProject(projectName, versions);
			
			if (addOr) {
				jqlClauseBuilder = jqlClauseBuilder.or();
			}
			else {
				addOr = true;
			}
			
			if (validVersions.size() > 0) {
				jqlClauseBuilder = jqlClauseBuilder.project(projectName).and().fixVersion().inStrings(validVersions);				
			}
			else {
				jqlClauseBuilder = jqlClauseBuilder.project(projectName);
			}
			
			if (userName != null && userName.trim().length() > 0) {
				jqlClauseBuilder.and().assigneeUser(userName);
			}
						
		}
		
		jqlClauseBuilder = jqlClauseBuilder.endsub();
		jqlClauseBuilder = jqlClauseBuilder.and().issueType(ISSUE_TYPE).and().status(STATUS_TYPE);
				
		return jqlClauseBuilder.endWhere().buildQuery();
	}
	
	protected boolean isProject() {
		return true;
	}
	
	protected Project findProject(String idOrName) {
		if (idOrName == null || idOrName.equals("")) {
			return null;
		}
		
		Project project = projectManager.getProjectObjByName(idOrName);
		if (project == null) {
			try {
				Long id = new Long(idOrName);
				return projectManager.getProjectObj(id);
			}catch (Exception e) {}
		}
		
		return project;
	}
	
	protected Collection<Version> findVersion(String idOrName) {
		if (idOrName == null || idOrName.equals ("")) {
			return null;
		}
		
		Collection<Version> versions = versionManager.getVersionsByName(idOrName);
		
		if (versions == null) {
			try {
				Long id = new Long(idOrName);
				Version version = versionManager.getVersion(id);
				if (version != null) {
					versions.add(version);						
				}
			}
			catch (Exception e) {}
		}
		
		return versions;
	}
	
	private Collection<String> findValidVersionsForProject(String projectName, Collection<String> versions) {
		Collection<String> c = new ArrayList<String>();		
		
		for (String versionName : versions) {
			Project project = ComponentManager.getInstance().getProjectManager().getProjectObjByName(projectName);
			long pid = project.getId();
			
			Version version = ComponentManager.getInstance().getVersionManager().getVersion(pid, versionName);
			if (version != null) {
				c.add(versionName);
			}
			
		}
		return c;
	}
	
	private Collection<String> buildStringCollection(String values) {
		Collection<String> c = new ArrayList<String>();
		
		if (values != null) {
			StringTokenizer st = new StringTokenizer(values, ",");
			while (st.hasMoreTokens()) {
				c.add(st.nextToken());
			}
		}
		
		return c;
	}
	
	private boolean validateProject(String projectName, User user) {
		if (projectName == null || projectName.equals("")) {
			return false;
		}
		
		Collection<String> projectNames = buildStringCollection(projectName);
		for (String name : projectNames) {
			if (findProject(name) == null) {
				return false;
			}
		}	
				
		return true;
	}
	
	private boolean validateVersion(String versionId, String projectId, User user) {
		if (projectId == null || projectId.equals("")) {
			return false;
		}
		
		Collection<String> versions = buildStringCollection(versionId);
		Collection<String> projects = buildStringCollection(projectId);
		Collection<String> validVersions = new ArrayList<String>();
		
		for (String projectName : projects) {
			validVersions.addAll(findValidVersionsForProject(projectName, versions));
		}
		
		for (String versionName : versions) {
			
			if (!validVersions.contains(versionName)) {
				return false;
			}
		}
		
		return true;
				
	}

}
