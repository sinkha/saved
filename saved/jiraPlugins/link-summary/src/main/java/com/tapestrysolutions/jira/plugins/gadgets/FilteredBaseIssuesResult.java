package com.tapestrysolutions.jira.plugins.gadgets;

import java.util.Collection;

import com.atlassian.jira.issue.Issue;

public class FilteredBaseIssuesResult {
	private Collection<Issue> linkedIssues;
	private Collection<Issue> unlinkedIssues;
	
	public FilteredBaseIssuesResult(Collection<Issue> linkedIssues, Collection<Issue> unlinkedIssues) {
		this.linkedIssues = linkedIssues;
		this.unlinkedIssues = unlinkedIssues;
	}
	
	public Collection<Issue> getLinkedIssues() {
		return linkedIssues;
	}
	
	public Collection<Issue> getUnlinkedIssues() {
		return unlinkedIssues;
	}
}
