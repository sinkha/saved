package com.tapestrysolutions.jira.plugins.customfield.linktypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;

@SuppressWarnings("unchecked")
public class LinkByTypesCFType extends CalculatedCFType {
	private JiraAuthenticationContext authenticationContext = null;
	private IssueLinkManager issueLinkManager = null;

	/**
	 * Picocontainer should populate this
	 * 
	 * @see "http://confluence.atlassian.com/display/JIRA/PicoContainer+and+JIRA"
	 *      />
	 * @param authenticationContext
	 *            authentication Context
	 * @param issueLinkManager
	 *            IssueLinkManager
	 */
	public LinkByTypesCFType(JiraAuthenticationContext authenticationContext,
			IssueLinkManager issueLinkManager) {
		this.authenticationContext = authenticationContext;
		this.issueLinkManager = issueLinkManager;
	}

	public Object getValueFromIssue(CustomField field, Issue issue) {
		String result = "";
		

		LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, authenticationContext.getLoggedInUser());
		
		Set<IssueLinkType> linkTypes = linkCollection.getLinkTypes();
		
		
		if (linkTypes != null && !linkTypes.isEmpty()) {
			for(IssueLinkType issueLinkType : linkTypes) {
				
				List<Issue> outwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
				if(outwardIssues != null && !outwardIssues.isEmpty()) {
					result+=issueLinkType.getOutward() + ": <br/><br/>";
					for (Issue outwardIssue : outwardIssues) {
						result+=outwardIssue.getKey() + "<br/>";
					}
				}
			}
		}
		
		return result;
//		User currentUser = authenticationContext.getLoggedInUser();
//		LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, currentUser);
//		System.out.println("linkCollection: " + linkCollection);
//		return linkCollection;
		
	}

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
		Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);
		
		LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, authenticationContext.getLoggedInUser());

		velocityParams.put("linkCollection", linkCollection);

		Set<IssueLinkType> linkTypes = linkCollection.getLinkTypes();

		velocityParams.put("linkTypes", linkTypes);

		return velocityParams;
    }
	
	/**
	 * not sure what this does...doesn't ever seem to get called...but must
	 * exist
	 */
	public String getStringFromSingularObject(Object customFieldObject) {
		return customFieldObject.toString();
	}

	/**
	 * not sure what this does...doesn't ever seem to get called...but must
	 * exist
	 */
	public Object getSingularObjectFromString(String customFieldObject) {
		return customFieldObject;
	}
}
