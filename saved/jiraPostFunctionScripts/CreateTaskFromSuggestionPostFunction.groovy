import com.atlassian.core.user.UserUtils
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.util.ImportUtils
import com.opensymphony.user.User
import com.opensymphony.workflow.WorkflowContext
import org.apache.log4j.Category
 
log = Category.getInstance("com.onresolve.jira.groovy.CreateDependentIssue")

issueMgr = ComponentManager.getInstance().getIssueManager()
projectMgr = ComponentManager.getInstance().getProjectManager()
Project project = issue.getProjectObject()

String currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
User currentUserObj = UserUtils.getUser(currentUser);
 
def wasIndexing = ImportUtils.indexIssues
ImportUtils.indexIssues = true
issueFactory = ComponentManager.getInstance().getIssueFactory()
newissue = issueFactory.getIssue()
newissue.setSummary(issue.summary)
newissue.setProject(project)
//new issue is a task
newissue.setIssueType(issue.getIssueType())
newissue.description = issue.description
newissue.reporter = issue.getReporter()
 
params = ["issue":newissue]
subTask = issueMgr.createIssue(currentUserObj, params)
println subTask.get("key")
 
// get the current list of outwards depends on links to get the sequence number
linkMgr = ComponentManager.getInstance().getIssueLinkManager()
def sequence = 0
for (IssueLink link in linkMgr.getInwardLinks(issue.id)) {
	if ("Relation" == link.issueLinkType.name) {
		sequence++;
	}
}
 
linkMgr = ComponentManager.getInstance().getIssueLinkManager()
// TODO: Should check that 10050 is always the link id for Relation
linkMgr.createIssueLink (newissue.id, issue.id, 10050, sequence, currentUserObj)
ImportUtils.indexIssues = wasIndexing