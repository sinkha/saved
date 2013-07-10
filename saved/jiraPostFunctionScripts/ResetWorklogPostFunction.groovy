import java.util.List
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.bc.JiraServiceContext
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult
import com.atlassian.jira.bc.issue.worklog.WorklogService
import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.issue.worklog.Worklog
import com.atlassian.jira.issue.worklog.WorklogManager
import com.opensymphony.user.User;
import com.atlassian.jira.util.JiraDurationUtils
import org.ofbiz.core.entity.GenericValue;

import org.apache.log4j.Category

log = Category.getInstance("com.tapestrysolutions.jira.groovy.ResetWorklogPostFunction")

ComponentManager componentManager = ComponentManager.getInstance()
JiraAuthenticationContext authenticationContext = componentManager.getJiraAuthenticationContext()
JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(authenticationContext.getUser())
WorklogService worklogService = componentManager.getComponentInstanceOfType(WorklogService.class)
JiraDurationUtils jiraDurationUtils = componentManager.getComponentInstanceOfType(JiraDurationUtils.class)
WorklogManager worklogManager = componentManager.getWorklogManager()
User user = authenticationContext.getUser()

if (jiraServiceContext!=null) {
	if(worklogService!=null) {
		if(worklogManager !=null) {
			for (Worklog worklog in worklogService.getByIssue(jiraServiceContext, issue)) {
				Long estimate = issue.getOriginalEstimate()
				worklogManager.delete(user, worklog, estimate, true)
			}
			// It appears that the process in which the 'timespent' gets updated, results
			// in an error when multiple worklogs are deleted. I can't quite figure out
			// why the 'timespent' value isn't getting updated correctly, but manually setting
			// the field takes care of it, so that's what I did.
			GenericValue issueGV = issue.getGenericValue();
			issueGV.set("timespent", 0L)
		}else {
			log.error "No WorklogManager found!"
		}
	}
	else {
		log.error "No WorklogService found!"
	}
	
}
else {
	log.error "No JiraServiceContext found!"
}

