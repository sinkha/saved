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

Long filterId = 15829;

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
	int idx = issue.summary.indexOf(" : ");
	if (idx != -1) {
		boolean wasIndexing = ImportUtils.isIndexIssues();
		ImportUtils.setIndexIssues(true);

		//String newSummary = issue.summary.substring(idx+3) + " : " + issue.summary.substring(0, idx);
		String newSummary =  issue.summary.replaceAll("\\.0\\.", "\\.00\\.");
		newSummary =  newSummary.replaceAll("\\.1\\.", "\\.01\\.");
		newSummary =  newSummary.replaceAll("\\.2\\.", "\\.02\\.");
		newSummary =  newSummary.replaceAll("\\.3\\.", "\\.03\\.");
		newSummary =  newSummary.replaceAll("\\.4\\.", "\\.04\\.");
		newSummary =  newSummary.replaceAll("\\.5\\.", "\\.05\\.");
		newSummary =  newSummary.replaceAll("\\.6\\.", "\\.06\\.");
		newSummary =  newSummary.replaceAll("\\.7\\.", "\\.07\\.");
		newSummary =  newSummary.replaceAll("\\.8\\.", "\\.08\\.");
		newSummary =  newSummary.replaceAll("\\.9\\.", "\\.09\\.");
		
		newSummary =  newSummary.replaceAll("\\.0\\s", "\\.00 ");
		newSummary =  newSummary.replaceAll("\\.1\\s", "\\.01 ");
		newSummary =  newSummary.replaceAll("\\.2\\s", "\\.02 ");
		newSummary =  newSummary.replaceAll("\\.3\\s", "\\.03 ");
		newSummary =  newSummary.replaceAll("\\.4\\s", "\\.04 ");
		newSummary =  newSummary.replaceAll("\\.5\\s", "\\.05 ");
		newSummary =  newSummary.replaceAll("\\.6\\s", "\\.06 ");
		newSummary =  newSummary.replaceAll("\\.7\\s", "\\.07 ");
		newSummary =  newSummary.replaceAll("\\.8\\s", "\\.08 ");
		newSummary =  newSummary.replaceAll("\\.9\\s", "\\.09 ");
		
		newSummary = newSummary.replaceFirst("^3\\.02", "3\\.2");
		try {
			issue.setSummary(newSummary)
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
}

["output":"$nModIssues issue(s) modified."]