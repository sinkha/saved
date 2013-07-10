import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.workflow.JiraWorkflow
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.opensymphony.workflow.loader.ActionDescriptor
import com.opensymphony.workflow.loader.StepDescriptor
import org.apache.log4j.Category
import com.atlassian.crowd.embedded.api.User


ComponentManager componentManager = ComponentManager.getInstance()
Category log = Category.getInstance(ResolveParentAfterSubtasks.class)
def projectManager = componentManager.getProjectManager()
public final static String FIELD_PARENTACTION = "FIELD_PARENTACTION"
public final static String FIELD_RESOLUTION_ID = "FIELD_RESOLUTION_ID"

String getName() {
	return "Transition parent when all subtasks are resolved"
}

String getDescription() {
	return """This will do the given action on the parent when all sub-tasks are resolved<br>
	"""
}

List getCategories() {
	["Function"]
}


Integer getActionId(Issue issue, String actionName) {
	JiraWorkflow workflow = componentManager.getWorkflowManager().getWorkflow(issue)
	StepDescriptor step = workflow.getLinkedStep(issue.status)
	ActionDescriptor ad = step.getActions().find {it.name == actionName} as ActionDescriptor
	ad?.id
}

List getParameters(Map params) {
	[
		[
			Name:FIELD_PARENTACTION,
			Label:"Parent action",
			Description:"Choose the action to do on the parent when the sub-tasks are resolved",
			Type: "list",
			Values: CannedScriptUtils.getAllWorkflowActions(false),
		],
		[
			Label:"Resolution",
			Name:FIELD_RESOLUTION_ID,
			Type: "list",
			Description:"Resolution to use on the parent",
			Values: CannedScriptUtils.getResolutionOptions(true),
		],
		// todo: need to allow a sub-task resolution
	]

}

public ErrorCollection doValidate(Map params, boolean forPreview) {
	// todo: check this is on a sub-task type
	null
}

Map doScript(Map params) {
	log.debug ("TestCondition.doScript with params: ${params}");

	String actionName = params[FIELD_PARENTACTION] as String
	MutableIssue subtask = params['issue'] as MutableIssue
	User user = WorkflowUtils.getUser(params)
	String resolutionId = params['Resolution'] as String

	log.debug ("actionName: $actionName")
	log.debug ("subtask: $subtask")
	log.debug ("subtask.isSubTask(): ${subtask.isSubTask()}")

	// if this action is resolve and all sub-tasks are resolved
	if (subtask.isSubTask()) {
		MutableIssue parent = subtask.getParentObject() as MutableIssue

		JiraWorkflow workflow = componentManager.getWorkflowManager().getWorkflow(parent)
		workflow.getLinkedStep(parent.status)
		
		boolean allResolved = parent.getSubTaskObjects().every {Issue issue ->
			issue.resolution
		}
		if (allResolved) {
			log.debug ("Resolve parent")
			Integer actionId = actionName?.replaceAll(/ .*/, "") as Integer
			if (WorkflowUtils.hasAction(parent, actionId)) {
				WorkflowUtils.resolveIssue(parent, actionId, user, resolutionId, [:])
			}
			else {
				log.warn("Action name: $actionName not found for this step.")
			}
		}
	}

	return params
}

String getDescription(Map params, boolean forPreview) {
	getName()
}

public Boolean isFinalParamsPage(Map params) {
	true
}
