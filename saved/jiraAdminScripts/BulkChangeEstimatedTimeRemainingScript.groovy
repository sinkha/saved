import com.atlassian.jira.ComponentManager
import com.atlassian.jira.ManagerFactory
import com.atlassian.jira.bc.JiraServiceContext
import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.index.IssueIndexManager
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import org.apache.log4j.Category
import com.atlassian.jira.util.BuildUtils
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.ofbiz.OfBizValueWrapper
import org.ofbiz.core.entity.GenericValue;

Long filterId = 15501;

ComponentManager componentManager = ComponentManager.getInstance()
JiraAuthenticationContext authenticationContext = componentManager.getJiraAuthenticationContext()
JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());
SearchProvider searchProvider = componentManager.getSearchProvider()
IssueManager issueManager = componentManager.getIssueManager()
IssueIndexManager indexManager = ComponentManager.getInstance().getIndexManager()
ConstantsManager constantsManager = componentManager.getConstantsManager()
def searchRequestService = componentManager.getSearchRequestService()


SearchRequest sr = searchRequestService.getFilter(ctx, filterId)
SearchResults results = searchProvider.search(sr.getQuery(), authenticationContext.getLoggedInUser(), PagerFilter.getUnlimitedFilter())
List issues = results.getIssues();

Long nModIssues = 0

for (Issue issue in issues)
{
	issue = issueManager.getIssueObject(issue.getId())

	boolean wasIndexing = ImportUtils.isIndexIssues();
	ImportUtils.setIndexIssues(true);

	try {
		issue.setOriginalEstimate(28800L)
		long remaining = 28800L
		if(issue.getTimeSpent() != null && issue.getTimeSpent() > 0L)
                	remaining = 28800L - issue.getTimeSpent()
		issue.setEstimate(remaining)
		issue.store()
		nModIssues++
	}
	finally {
		if ((BuildUtils.getCurrentBuildNumber() as Long) < 614) {
			ManagerFactory.getCacheManager().flush(com.atlassian.jira.issue.cache.CacheManager.ISSUE_CACHE, issue)
		}
		indexManager.reIndex(issue);
		ImportUtils.setIndexIssues(wasIndexing);
	}

}
["output":"$nModIssues issue(s) modified."]