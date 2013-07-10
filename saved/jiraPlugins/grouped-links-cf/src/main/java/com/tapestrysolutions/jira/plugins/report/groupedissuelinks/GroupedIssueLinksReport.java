package com.tapestrysolutions.jira.plugins.report.groupedissuelinks;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
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
import com.opensymphony.util.TextUtils;
import com.atlassian.jira.issue.CustomFieldManager;

public class GroupedIssueLinksReport extends AbstractReport {

	private static Log log = LogFactory.getLog(GroupedIssueLinksReport.class);
	private final static String FILTER = "filter";

	private final SearchProvider searchProvider;
	private final JiraAuthenticationContext authenticationContext;
	private final SearchRequestService searchRequestService;
	private final IssueManager issueManager;
	private final IssueLinkManager issueLinkManager;
	private final IssueFactory issueFactory;
	private final ProjectManager projectManager;
	private final ChangeHistoryManager changeHistoryManager;
	private final IssueIndexManager issueIndexManager;
	private final SearchService searchService;
	private final FieldVisibilityManager fieldVisibilityManager;
	private final ReaderCache readerCache;
	private final CustomFieldManager customFieldManager;

	public GroupedIssueLinksReport(
			final SearchProvider searchProvider,
			final JiraAuthenticationContext authenticationContext,
			final SearchRequestService searchRequestService,
			final IssueFactory issueFactory,
			final IssueManager issueManager,
			final IssueLinkManager issueLinkManager,
			final ChangeHistoryManager changeHistoryManager,
			final ProjectManager projectManager,
			final IssueIndexManager issueIndexManager,
			final SearchService searchService,
			final FieldVisibilityManager fieldVisibilityManager,
			final ReaderCache readerCache,
			final CustomFieldManager customFieldManager) {
		this.searchProvider = searchProvider;
		this.authenticationContext = authenticationContext;
		this.searchRequestService = searchRequestService;
		this.issueManager = issueManager;
		this.issueLinkManager = issueLinkManager;
		this.issueFactory = issueFactory;
		this.changeHistoryManager = changeHistoryManager;
		this.projectManager = projectManager;
		this.issueIndexManager = issueIndexManager;
		this.searchService = searchService;
		this.fieldVisibilityManager = fieldVisibilityManager;
		this.readerCache = readerCache;
		this.customFieldManager = customFieldManager;
	}

	@SuppressWarnings("unchecked")
	public String generateReport(ProjectActionSupport action, @SuppressWarnings("rawtypes") Map params, boolean isExcel)
			throws Exception {
		String projectOrFilterId = (String) params.get("projectOrFilterId");
		if (projectOrFilterId == null) {
		log.error("Grouped Issue Links Report run without a project selected: params="
				+ params);
		return "<span class='errMsg'>No search filter has been selected. Please "
				+ "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
				+ TextUtils.htmlEncode((String) params
						.get("selectedProjectId"))
				+ "\">create one</a>, and re-run this report.</span>";
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
					+ "\">create one</a>, and re-run this report.</span>";
		}
		
		final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(
				authenticationContext.getUser());
		final SearchRequest request = searchRequestService.getFilter(jiraServiceContext,
				new Long(selectedProjectId));
		final Long projectId = new Long(selectedProjectId);
		List<Issue> issues = null;
		
		if (projectId != null && projectId.longValue() > -1) {
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
			SearchRequest filter = isFilter ? searchRequestService.getFilter(jiraServiceContext, projectId):
												new SearchRequest(queryBuilder.where().project(projectId).buildQuery());
			if (filter != null) { // not logged in
				try {
					SearchResults searchResults = searchProvider.search(filter.getQuery(),
						authenticationContext.getLoggedInUser(),
						PagerFilter.getUnlimitedFilter());
				
					issues = searchResults.getIssues();

				} catch (SearchException e) {
					e.printStackTrace();
				}
			} else {
				return "<span class='errMsg'>No search filter has been selected. Please "
						+ "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
						+ TextUtils.htmlEncode((String) params
								.get("selectedProjectId"))
						+ "\">create one</a>, and re-run this report.</span>";
			}
		}
		
		
		
		@SuppressWarnings("rawtypes")
		final Map startingParams;
		startingParams = EasyMap
				.build("action", action,
						"issues", issues,
						"issueLinkManager", issueLinkManager,
						"customFieldManager", customFieldManager,
						"authenticationContext", authenticationContext,
						"issueManager", issueManager,
						"searchRequest", request,
						"isExcel", isExcel,
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
}
