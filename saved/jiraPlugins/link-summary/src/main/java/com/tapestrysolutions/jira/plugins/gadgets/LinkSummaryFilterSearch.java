package com.tapestrysolutions.jira.plugins.gadgets;

public class LinkSummaryFilterSearch extends LinkSummarySearchBase {

	@Override
	public String getFunctionName() {
		return "linkSummaryFilterSearch";
	}

	@Override
	protected boolean isProject() {
		return false;
	}

}
