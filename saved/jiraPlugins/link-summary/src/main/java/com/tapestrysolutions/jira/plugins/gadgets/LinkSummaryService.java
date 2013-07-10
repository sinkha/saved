package com.tapestrysolutions.jira.plugins.gadgets;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.User;

import org.ofbiz.core.entity.GenericValue;
 
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
 
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
 
@Path ("/linkSummary")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class LinkSummaryService
{
	
	public final String COMPONENT = "component";
    public final String ASSIGNEE = "assignee";
    public final String FIX_VERSION = "fixVersion";
    public final String RESOLUTION = "resolution";
    public final String STATUS = "status";
    public final String FILTER = "filter-";
    public final String PROJECT = "project-";
    public final String ROW_TOTALS = "rowTotals";
    public final String DESCENDING = "descending";
 
    private final VersionManager versionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    
    private final ConstantsManager constantsManager;
 
    public LinkSummaryService(final SearchService searchService, final JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory, final VersionManager versionManager, final ConstantsManager constantsManager)
    {
        this.searchService = searchService;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.versionManager = versionManager;
        this.constantsManager = constantsManager;
    }
    
    @GET
    @Path ("/issueTypes")
    
    public Response getAllIssueTypes() {
    	ArrayList<String> list = new ArrayList<String>();
    	Collection<IssueType> issueTypes = constantsManager.getAllIssueTypeObjects();
    	
    	for (IssueType issueType : issueTypes) {
    		list.add(issueType.getName() + "");
    	}
    	
    	return Response.ok(new IssueTypeReturn(list)).cacheControl(NO_CACHE).build();
    }
    
    @GET
    @Path ("/test_resp")
    
    public Response testResp(@QueryParam ("input") String input) {
    	return Response.ok(new TestOut(input + " output")).cacheControl(NO_CACHE).build();
    }
    
    @GET
    @Path ("/linkAnalysis")
    
    public Response linkAnalysis(@QueryParam ("searchId") String searchId, @QueryParam("isProject") boolean isProject, @QueryParam("groupField") String groupField, @QueryParam("sortBy") String sortBy, @QueryParam("sortDirection") String sortDirection, @QueryParam("baseIssueType") String baseIssueType, @QueryParam("linkIssueType") String linkIssueType, @QueryParam("customFieldChecked") String customFieldChecked) {
    	LinkSummaryResponse summary = new LinkSummaryResponse();
    	
    	if (searchId == null || groupField == null || sortBy == null || sortDirection == null || baseIssueType == null || linkIssueType == null) {
    		return Response.ok(summary).cacheControl(NO_CACHE).build();
    	}
    	
    	if (customFieldChecked == null) {
    		customFieldChecked="";
    	}
    	
    	User user = authenticationContext.getUser();
    	LinkSummarySearchBase searcher;
    	if (isProject) {
    		searcher = new LinkSummaryProjectSearch();
    	}
    	else {
    		searcher = new LinkSummaryFilterSearch();
    	}
    	List<Issue> baseIssuesAll = searcher.getAllBaseIssues(searchId, baseIssueType, user);
    	FilteredBaseIssuesResult result = searcher.getFilteredIssues(baseIssuesAll, linkIssueType, customFieldChecked, user);
    	Collection<Issue> baseIssuesLinked = result.getLinkedIssues();
    	Collection<Issue> baseIssuesUnlinked = result.getUnlinkedIssues();
    	
    	for (Issue issue : baseIssuesLinked) {
    		Collection<String> rowNames = getRowNames(issue, groupField);
    		for (String rowName : rowNames) {
    			summary.getOrCreateRow(rowName).incLinkedCount();
    		}
    	}
    	for (Issue issue : baseIssuesUnlinked) {
    		Collection<String> rowNames = getRowNames(issue, groupField);
    		for (String rowName : rowNames) {
    			summary.getOrCreateRow(rowName).incNotLinkedCount();
    		}
    	}
    	summary.modifyTotalLinked(baseIssuesLinked.size());
    	summary.modifyTotalNotLinked(baseIssuesUnlinked.size());
    	applyJqlStrings(summary, searcher.getFunctionName(), searchId, groupField, baseIssueType, linkIssueType, customFieldChecked);
    	
    	if (sortBy.equals(ROW_TOTALS)) {
    		Collections.sort(summary.getRows());
    	}
    	if (sortDirection.equals(DESCENDING)) {
    		Collections.reverse(summary.getRows());
    	}
    	
    	return Response.ok(summary).cacheControl(NO_CACHE).build();
    }
    
    private void applyJqlStrings(LinkSummaryResponse summary, String searcher, String searchBase, String groupField, String baseIssueType, String linkIssueType, String customFieldChecked) {
    	String baseLink = "issue in " + searcher + "(\"" + searchBase + "\", \"" + baseIssueType + "\", \"" + linkIssueType + "\", \"" + customFieldChecked + "\"  )";
    	String baseUnlink = "issue in " + searcher + "(\"" + searchBase + "\", \"" + baseIssueType + "\", \"" + linkIssueType + "\", \"" + customFieldChecked + "\", \"false\")";
    	try {
    		summary.modifyLinkJql(URLEncoder.encode(baseLink, "UTF-8"));
    		summary.modifyNotLinkJql(URLEncoder.encode(baseUnlink, "UTF-8"));
    	}catch(Exception e) {}
    	for (LinkSummaryRow row : summary.getRows()) {
    		String link = baseLink + " and " + groupField + " = \"" + row.getName() + "\"";
    		String unlink = baseUnlink + " and " + groupField + " = \"" + row.getName() + "\"";
    		
    		try {
    			row.modifyLinkJql(URLEncoder.encode(link, "UTF-8"));
    			row.modifyNotLinkJql(URLEncoder.encode(unlink, "UTF-8"));
    		}catch(Exception e) {}
    		
    	}
    }
    
    public Collection<String> getRowNames(Issue issue, String nameType) {
    	ArrayList<String> out = new ArrayList<String>();
    	
    	if (nameType.equals(COMPONENT)) {
    		Collection<GenericValue> components = issue.getComponents();
    		for (GenericValue component : components) {
    			out.add((String)component.get("name"));
    		}
    	}
    	else if (nameType.equals(ASSIGNEE)) {
    		issue.getAssignee();
    		out.add(issue.getAssignee().getFullName());
    	}
    	else if (nameType.equals(FIX_VERSION)) {
    		Collection<Version> fixVersions = issue.getFixVersions();
    		for (Version fixVersion : fixVersions) {
    			out.add(fixVersion.getName());
    		}
    	}
    	else if (nameType.equals(RESOLUTION)) {
    		if (issue.getResolutionObject() != null) {
    			out.add(issue.getResolutionObject().getName());	
    		}
    		else {
    			out.add("Unresolved");
    		}
    		
    	}
    	else if (nameType.equals(STATUS)) {
    		out.add(issue.getStatusObject().getName());
    	}
    	
    	if (out.size() == 0) {
    		out.add("None");
    	}
    	
    	return out;
    }
    ///CLOVER:OFF
    
    
    @XmlRootElement
    public static class LinkSummaryResponse {
    	@XmlElement
    	private ArrayList<LinkSummaryRow> rows;
    	
    	@XmlElement
    	private long totalLinked;
    	
    	@XmlElement
    	private long totalNotLinked;
    	
    	@XmlElement
    	private String linkJql;
    	
    	@XmlElement
    	private String notLinkJql;
    	
    	//private LinkSummaryResponse() {}
    	
    	LinkSummaryResponse() {
    		this.rows = new ArrayList<LinkSummaryRow>();//= rows;
    		this.totalLinked = 0;
    		this.totalNotLinked = 0;
    	}
    	
    	public void modifyLinkJql(String value) {
    		linkJql = value;
    	}
    	
    	public void modifyNotLinkJql(String value) {
    		notLinkJql = value;
    	}
    	
    	public ArrayList<LinkSummaryRow> getRows() {
    		return rows;
    	}
    	
    	public long getTotalLinked() {
    		return totalLinked;
    	}
    	
    	public long getTotalNotLinked() {
    		return totalNotLinked;
    	}
    	
    	public void modifyTotalLinked(long value) {
    		totalLinked = value;
    	}
    	
    	public void modifyTotalNotLinked(long value) {
    		totalNotLinked = value;
    	}
    	
    	public LinkSummaryRow getOrCreateRow(String rowName) {
        	LinkSummaryRow temp = new LinkSummaryRow(rowName);
        	int index = rows.indexOf(temp);
        	if (index == -1) {
        		rows.add(temp);
        	}
        	else {
        		temp = rows.get(index);
        	}
        	return temp;
        }
    }
    
    @XmlRootElement
    public static class LinkSummaryRow implements Comparable<LinkSummaryRow>{
    	@XmlElement
    	private String name;
    	
    	@XmlElement
    	private long linkedCount;
    	
    	@XmlElement
    	private long notLinkedCount;
    	
    	@XmlElement
    	private String linkJql;
    	
    	@XmlElement
    	private String notLinkJql;
    	
    	private LinkSummaryRow() {}
    	
    	public LinkSummaryRow(String name) {
    		this.name = name;
    		this.linkedCount = 0;
    		this.notLinkedCount = 0;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public void modifyLinkJql(String value) {
    		linkJql = value;
    	}
    	
    	public void modifyNotLinkJql(String value) {
    		notLinkJql = value;
    	}
    	
    	public long getLinkedCount() {
    		return linkedCount;
    	}
    	
    	public void incLinkedCount() {
    		++linkedCount;
    	}
    	
    	public long getNotLinkedCount() {
    		return notLinkedCount;
    	}
    	
    	public void incNotLinkedCount() {
    		++notLinkedCount;
    	}
    	
    	public int compareTo(LinkSummaryRow o) {
    		return (int) ((this.getLinkedCount() + this.getNotLinkedCount()) - (o.getLinkedCount() + o.getNotLinkedCount()));
    	}
    	
    	public boolean equals(Object o) {
    		if (o instanceof LinkSummaryRow) {
    			return ((LinkSummaryRow) o).getName().equals(this.getName());
    		}
    		return false;
    	}
    }
    
    @XmlRootElement
    public static class IssueTypeReturn {
    	@XmlElement
    	Collection<String> issueTypes;
    	
    	private IssueTypeReturn() {}
    	
    	IssueTypeReturn(Collection<String> issueTypes) {
    		this.issueTypes = issueTypes;
    	}
    }
    
    @XmlRootElement
    public static class TestOut {
    	@XmlElement
    	String resp;
    	
    	private TestOut() {}
    	
    	TestOut(String resp) {
    		this.resp = resp;
    	}
    	
    	public String getResp() {
    		return resp;
    	}
    }
}