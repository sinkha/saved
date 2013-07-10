package com.tapestrysolutions.jira.plugins.gadgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.opensymphony.user.User;

public abstract class LinkSummarySearchBase implements JqlFunction {
	
	private final int MIN_NUMBER_OF_ARGS = 4;
	
	protected JqlFunctionModuleDescriptor descriptor;
	
	protected final SearchRequestService searchRequestService;
	protected final ProjectManager projectManager;
	protected final ConstantsManager constantsManager;
	protected final SearchProvider searchProvider;
	protected final IssueLinkManager issueLinkManager;
	protected final CustomFieldManager customFieldManager;
	
	public LinkSummarySearchBase() {
		ComponentManager cm = ComponentManager.getInstance();
		
		searchRequestService = cm.getSearchRequestService();
		projectManager = cm.getProjectManager();
		constantsManager = cm.getConstantsManager();
		searchProvider = cm.getSearchProvider();
		issueLinkManager = cm.getIssueLinkManager();
		customFieldManager = cm.getCustomFieldManager();
	}
	
	@Override
	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		
		MessageSet messages = new MessageSetImpl();
        List<String> arguments = operand.getArgs();

        //Make sure we have the correct number of arguments.
        if (arguments.size() < MIN_NUMBER_OF_ARGS)
        {
            messages.addErrorMessage("Not enough arguments provided");
            return messages;
        }
        
        if (!validateFilterOrProject(arguments.get(0), searcher)) {
        	if (isProject()) {
        		messages.addErrorMessage("Unable to find project");
        	}
        	else {
        		messages.addErrorMessage("Unable to find filter");
        	}
        }

        ArrayList<String> issueTypes = getIssueTypes(arguments.get(1));        
        for (String issueType : issueTypes) {
        	if (!validateIssueType(issueType)) {
            	messages.addErrorMessage("Issue type " + issueType + " does not exist");
            }
        	
        }
        
        issueTypes = getIssueTypes(arguments.get(2));        
        for (String issueType : issueTypes) {
        	if (!validateIssueType(issueType)) {
            	messages.addErrorMessage("Issue type " + issueType + " does not exist");
            }
        	
        }
        
        String customFieldName = arguments.get(3);
        if (customFieldName != null && customFieldName.length() > 0) {
            if (!validateCustomField(customFieldName)) {
            	messages.addErrorMessage("Custom field type " + customFieldName + " does not exist");
            }        	
        }

        return messages;
	}
	
	private boolean validateFilterOrProject(String id, User user) {
		if (id == null || id.equals("")) {
			return false;
		}
		
		if (isProject()) {
			return (findProject(id) != null);
		}
		else {
			return (findFilter(id, user) != null);
		}
	}
	
	@Override
	public List<QueryLiteral> getValues(QueryCreationContext queryCreationContext,
			FunctionOperand operand, TerminalClause terminalClause) {
		
		User user = queryCreationContext.getUser();
		List<String> arguments = operand.getArgs();
		
		if (arguments.size() < MIN_NUMBER_OF_ARGS) {
			return new ArrayList<QueryLiteral>();
		}
		
		String filterOrProject = arguments.get(0);
		String baseIssueType = arguments.get(1);
		String linkIssueType = arguments.get(2);
		String customFieldChecked = arguments.get(3);
		boolean isLinked = arguments.size() > MIN_NUMBER_OF_ARGS ? new Boolean(arguments.get(4)) : true;

		
		Collection<Issue> allBaseIssues = getAllBaseIssues(filterOrProject, baseIssueType, user);
		FilteredBaseIssuesResult filteredResult = getFilteredIssues(allBaseIssues, linkIssueType, customFieldChecked, user);
		Collection<Issue> filteredIssues;
		if (isLinked) {
			filteredIssues = filteredResult.getLinkedIssues();
		}
		else {
			filteredIssues = filteredResult.getUnlinkedIssues();
		}
		return getQueryLiteralArrayList(filteredIssues, operand);
	}
	
	public List<Issue> getAllBaseIssues(String filterOrProject, String baseIssueType, User user) {
		Query query = getQuery(filterOrProject, baseIssueType);
		try {
			return searchProvider.search(query, user, PagerFilter.getUnlimitedFilter()).getIssues();
		}catch(Exception e) {}
		return new ArrayList<Issue>();
	}
	
	public FilteredBaseIssuesResult getFilteredIssues(Collection<Issue> baseIssues, String linkIssueType, String customFieldChecked, User user) {
		ArrayList<Issue> linked = new ArrayList<Issue>();
		ArrayList<Issue> unlinked = new ArrayList<Issue>();
		Collection<Issue> linkIssues;
		
		
		
		ArrayList<String> linkIssueTypes = getIssueTypes(linkIssueType);

		
		for (Issue baseIssue : baseIssues) {
			linkIssues = issueLinkManager.getLinkCollection(baseIssue, user).getAllIssues();
			boolean includeInLinked = false;
			
			if (customFieldIsNotEmpty(baseIssue, customFieldChecked)) {
				includeInLinked = true;
			}
			else {

				for (Issue linkIssue : linkIssues) {
					if (linkIssueTypes.contains(linkIssue.getIssueTypeObject().getName())) {
						includeInLinked = true;
						break;
					}
				}

			}
			
			if (includeInLinked) {
				linked.add(baseIssue);
			}
			else {
				unlinked.add(baseIssue);
			}
		}
		return new FilteredBaseIssuesResult(linked, unlinked);
	}
	
	protected boolean customFieldIsNotEmpty(Issue issue, String customFieldName) {
		for (CustomField customField: customFieldManager.getCustomFieldObjects(issue)) {
			if (customFieldName != null && customField.getName().equals(customFieldName)) {

				Object value = issue.getCustomFieldValue(customField);
				if (value != null) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		
		return false;
		
		
	}	
	
	public ArrayList<QueryLiteral> getQueryLiteralArrayList(Collection<Issue> issues, FunctionOperand operand) {
		ArrayList<QueryLiteral> output = new ArrayList<QueryLiteral>();
		for (Issue issue : issues) {
			output.add(new QueryLiteral(operand, issue.getId()));
		}
		if (output.size() == 0) {
			output.add(new QueryLiteral());
		}
		return output;
	}
	
	@Override
	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}
	
	@Override
	public int getMinimumNumberOfExpectedArguments() {
		return MIN_NUMBER_OF_ARGS;
	}
	
	@Override
	public boolean isList() {
		return true;
	}
	
	@Override
	public void init(JqlFunctionModuleDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	protected ArrayList<String> getIssueTypes(String issueType) {
		ArrayList<String> linkIssueTypes = new ArrayList<String>();
		
		if (issueType.contains(",")) {
			String[] result = issueType.split(",");
			for (int x=0; x<result.length; x++) {
				linkIssueTypes.add(result[x].trim());
			}			
		}
		else {
			linkIssueTypes.add(issueType);
		}
		
		return linkIssueTypes;
		
	}
	
	protected boolean validateCustomField(String customFieldName) {
		Collection<CustomField> cfs = customFieldManager.getCustomFieldObjectsByName(customFieldName);
		
		if (cfs == null || cfs.size() == 0) {
			return false;
		}
		
		return true;
	}
	
	protected boolean validateIssueType(String issueType) {
		for (IssueType it : constantsManager.getAllIssueTypeObjects()) {
			if (it.getName().equals(issueType)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected SearchRequest findFilter(String idOrName, User user) {
		if (idOrName == null || idOrName.equals("")) {
			return null;
		}
		
		SharedEntitySearchParametersBuilder pbuilder = new SharedEntitySearchParametersBuilder();
		pbuilder.setName(idOrName);
		List<SearchRequest> filters = searchRequestService.search(new JiraServiceContextImpl(user), pbuilder.toSearchParameters(), 0, Integer.MAX_VALUE).getResults();
		for (SearchRequest filter : filters) {
			if (filter.getName().equals(idOrName)) {
				return filter;
			}
		}
		
		try {
			Long id = new Long(idOrName);
			return searchRequestService.getFilter(new JiraServiceContextImpl(user), id);
		}catch (Exception e) {}
		
		return null;
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
	
	protected Query getQuery(String projectOrFilter, String baseType) {
		JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
		builder.clear();
		if (isProject()) {
			builder.where().project(projectOrFilter).and().issueType(baseType);
		}
		else {
			builder.where().savedFilter(projectOrFilter).and().issueType(baseType);
		}
		
		return builder.buildQuery();
	}
	
	/**
	 * Used to determine if the first parameter is a project or filter
	 * @return true if first parameter is a project, false if it is a filter
	 */
	protected abstract boolean isProject();
}
