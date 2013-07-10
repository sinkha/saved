package com.tapestrysolutions.jira.plugins.report.reopenedbugs;

import java.util.Collection;

import com.atlassian.jira.issue.Issue;

public class FilteredBaseIssuesResult {
	private Collection<Issue> bugsFixed;
	private Collection<Issue> bugsReopened;
	
	public FilteredBaseIssuesResult(Collection<Issue> bugsFixed, Collection<Issue> bugsReopened) {
		this.bugsFixed = bugsFixed;
		this.bugsReopened = bugsReopened;
	}
	
	public Collection<Issue> getBugsFixed() {
		return bugsFixed;
	}
	
	public Collection<Issue> getBugsReopened() {
		return bugsReopened;
	}
}
