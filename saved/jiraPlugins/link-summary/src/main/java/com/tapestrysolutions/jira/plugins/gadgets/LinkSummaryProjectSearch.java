package com.tapestrysolutions.jira.plugins.gadgets;

public class LinkSummaryProjectSearch extends LinkSummarySearchBase {

	@Override
	public String getFunctionName() {
		return "linkSummaryProjectSearch";
	}

	@Override
	protected boolean isProject() {
		return true;
	}

}
