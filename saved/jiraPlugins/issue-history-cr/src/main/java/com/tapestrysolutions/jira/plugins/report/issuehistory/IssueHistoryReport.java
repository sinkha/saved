package com.tapestrysolutions.jira.plugins.report.issuehistory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.HitCollector;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;

public class IssueHistoryReport extends AbstractReport {

	private static Log log = LogFactory.getLog(IssueHistoryReport.class);
	private final static String FILTER = "filter";

	private final SearchProvider searchProvider;
	private final JiraAuthenticationContext authenticationContext;
	private final SearchRequestService searchRequestService;
	private final IssueManager issueManager;
	private final IssueFactory issueFactory;
	private final ProjectManager projectManager;
	private final ChangeHistoryManager changeHistoryManager;
	private final IssueIndexManager issueIndexManager;
	private final SearchService searchService;
	private final FieldVisibilityManager fieldVisibilityManager;
	private final ReaderCache readerCache;

	public IssueHistoryReport(
			final SearchProvider searchProvider,
			final JiraAuthenticationContext authenticationContext,
			final SearchRequestService searchRequestService,
			final IssueFactory issueFactory,
			final IssueManager issueManager,
			final ChangeHistoryManager changeHistoryManager,
			final ProjectManager projectManager,
			final IssueIndexManager issueIndexManager,
			final SearchService searchService,
			final FieldVisibilityManager fieldVisibilityManager,
			final ReaderCache readerCache) {
		this.searchProvider = searchProvider;
		this.authenticationContext = authenticationContext;
		this.searchRequestService = searchRequestService;
		this.issueManager = issueManager;
		this.issueFactory = issueFactory;
		this.changeHistoryManager = changeHistoryManager;
		this.projectManager = projectManager;
		this.issueIndexManager = issueIndexManager;
		this.searchService = searchService;
		this.fieldVisibilityManager = fieldVisibilityManager;
		this.readerCache = readerCache;
	}

	@SuppressWarnings("unchecked")
	public String generateReport(ProjectActionSupport action, @SuppressWarnings("rawtypes") Map params, boolean isExcel)
			throws Exception {
		String projectOrFilterId = (String) params.get("projectOrFilterId");
		if (projectOrFilterId == null) {
		log.error("Single Level Group By Report run without a project selected (JRA-5042): params="
				+ params);
		return "<span class='errMsg'>No search filter has been selected. Please "
				+ "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
				+ TextUtils.htmlEncode((String) params
						.get("selectedProjectId"))
				+ "\">create one</a>, and re-run this report. See also "
				+ "<a href=\"http://jira.atlassian.com/browse/JRA-5042\">JRA-5042</a></span>";
		}
		
		String[] parsed = projectOrFilterId.trim().split("-");
		String selectedProjectId;
		boolean isFilter;
		if(parsed.length == 2) {
			isFilter = FILTER.equals(parsed[0]);
			selectedProjectId = parsed[1];
		}
		else {
			return "<span class='errMsg'>No search filter has been selected. Please "
					+ "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
					+ TextUtils.htmlEncode((String) params
							.get("selectedProjectId"))
					+ "\">create one</a>, and re-run this report. See also "
					+ "<a href=\"http://jira.atlassian.com/browse/JRA-5042\">JRA-5042</a></span>";
		}
		
		final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(
				authenticationContext.getUser());
		final SearchRequest request = searchRequestService.getFilter(jiraServiceContext,
				new Long(selectedProjectId));
		final Long projectId = new Long(selectedProjectId);
		
		Set<Long> filteredIssues = new TreeSet<Long>();
		Map<Long, Integer> issueToHistoryChangeNumber = new HashMap<Long, Integer>();
		if (projectId != null && projectId.longValue() > -1) {
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
			SearchRequest filter = isFilter ? searchRequestService.getFilter(jiraServiceContext, projectId):
												new SearchRequest(queryBuilder.where().project(projectId).buildQuery());
			if (filter != null) { // not logged in
				try {
				SearchResults issues = searchProvider.search(filter.getQuery(),
						authenticationContext.getLoggedInUser(),
						PagerFilter.getUnlimitedFilter());
				
				for (Iterator<Issue> i = issues.getIssues().iterator(); i.hasNext();) {
					Issue value = i.next();
					
					List<ChangeHistory> histories = this.changeHistoryManager.getChangeHistories(value);
					int count = 0;
					for(ChangeHistory hist : histories) {
						@SuppressWarnings("rawtypes")
						List list = hist.getChangeItems();
						for(Object obj : list) {
							if(obj instanceof GenericValue) {
								count++;
							}
						}
					}
					issueToHistoryChangeNumber.put(value.getId(), count);
					filteredIssues.add(value.getId());
				}
				} catch (SearchException e) {
					e.printStackTrace();
				}
			} else {
				return "<span class='errMsg'>No search filter has been selected. Please "
						+ "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
						+ TextUtils.htmlEncode((String) params
								.get("selectedProjectId"))
						+ "\">create one</a>, and re-run this report. See also "
						+ "<a href=\"http://jira.atlassian.com/browse/JRA-5042\">JRA-5042</a></span>";
			}
		}
		
		@SuppressWarnings("rawtypes")
		final Map startingParams;
		startingParams = EasyMap
				.build("action",
						action,
						"filteredIssues", filteredIssues,
						"issueToHistoryChangeNumber", issueToHistoryChangeNumber,
						"issueManager", issueManager,
						"changeHistoryManager", changeHistoryManager,
						"searchRequest", request,
						"fieldVisibility", new FieldVisibilityBean(),
						"project", !isFilter ? projectManager.getProjectObj(new Long(selectedProjectId)) : null,
						"searchService", searchService, "portlet", this);

		return descriptor.getHtml("view", startingParams);
	}
	
	// Generate html report
    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        return generateReport(action, params, false);
    }

    // Generate excel, report
    public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception {
        return generateReport(action, params, true);
    }
	
	public boolean isExcelViewSupported() {
        return true;
    }
	
	private void getIssues(Long projectId, boolean isFilter) {
		
		JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(authenticationContext.getUser());
		
		Set<Long> filteredIssues = new TreeSet<Long>();
		if (projectId != null && projectId.longValue() > -1) {
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
	        Query query = queryBuilder.where().project(projectId).buildQuery();
			SearchRequest filter = isFilter ? searchRequestService.getFilter(jiraServiceContext, projectId):
												new SearchRequest(query);
			if (filter != null) { // not logged in
				try {
				SearchResults issues = searchProvider.search(filter.getQuery(),
						authenticationContext.getLoggedInUser(),
						PagerFilter.getUnlimitedFilter());
				
				for (Iterator<Issue> i = issues.getIssues().iterator(); i
						.hasNext();) {
					Issue value = i.next();
					filteredIssues.add(value.getId());
				}
				} catch (SearchException e) {
					e.printStackTrace();
				}
			} else {
				return;
			}
		}
		
		for(Long id : filteredIssues) {
			Issue issue = issueManager.getIssueObject(id);
			List<ChangeHistory> histories = this.changeHistoryManager.getChangeHistories(issue);
			String loggers = issue.getKey();
			issue.getIssueTypeObject().getName();
			issue.getDescription();
			for(ChangeHistory hist : histories) {
				if(hist == null) {
					continue;
				}
				String userName;
				if(hist.getAuthorUser()!=null) {
					userName = hist.getAuthorUser().getName();
				}
				else {
					userName = "N/A";
				}
				loggers = loggers + ", " + userName + ":" + hist.getTimePerformed();
				List list = hist.getChangeItems();
				for(Object obj : list) {
					if(obj instanceof GenericValue) {
						System.out.println("Field: " + ((GenericValue)obj).get("field") + " NewValue: " + ((GenericValue)obj).get("newstring") + " OldValue: " + ((GenericValue)obj).get("oldstring"));
					}
					else {
						System.out.println("WHATTERS!!: " + obj);
					}
				}
				System.out.println("Next History.\n\n");
			}
		}
	}

}
