package com.tapestrysolutions.jira.plugins.gadgets;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.User;

import org.ofbiz.core.entity.GenericValue;
 
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
 
@Path ("/hourSummary")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class HourSummaryService
{
	
	private final String COMPONENT = "component";
    private final String ASSIGNEE = "assignee";
    private final String FIX_VERSION = "fixVersion";
    private final String ISSUE_TYPE	= "issueType";
    private final String AFFECTED_VERSION = "affectedVersion";
    private final String STATUS = "status";
    private final String PRIORITY = "priority";
    private final String FILTER = "filter-";
    private final String PROJECT = "project-";
    private final String REMAINING_ESTIMATE = "remainingEstimate";
    private final String DESCENDING = "descending";
 
    private final VersionManager versionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    
    private final IssueManager issueManager;
    private final SearchRequestService searchRequestService;
    private final SearchProvider searchProvider;
    private final ProjectManager projectManager;
    private final ApplicationProperties applicationProperties;
 
    public HourSummaryService(final SearchService searchService, final JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory, final VersionManager versionManager, final IssueManager issueManager, final SearchRequestService searchRequestService, final SearchProvider searchProvider, final ProjectManager projectManager, final ApplicationProperties applicationProperties)
    {
        this.searchService = searchService;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.versionManager = versionManager;
        this.issueManager = issueManager;
        this.searchRequestService = searchRequestService;
        this.searchProvider = searchProvider;
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
    }
    
    @GET
    @Path ("/test_resp")
    
    public Response testResp(@QueryParam ("input") String input) {
    	return Response.ok(new TestOut(input + " output")).cacheControl(NO_CACHE).build();
    }
    
    @GET
    @Path ("/hourAnalysis")
    
    public Response hourAnalysis(@QueryParam ("searchId") String searchId, @QueryParam("groupField") String groupField, @QueryParam("sortBy") String sortBy, @QueryParam("sortDirection") String sortDirection) {
    	int hoursPerDay = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
    	int daysPerWeek = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
    	HourSummaryResponse summary = new HourSummaryResponse();
    	summary.doSearchName(getSearchDisplayName(searchId));
    	List<Issue> issues = getApplicableIssues(searchId);
    	
    	for (Issue issue : issues) {
    		long remaining = getRemaining(issue);
    		long estimate = getEstimate(issue);
    		long spent = getSpent(issue);
    		
    		Collection<String> rowNames = getRowNames(issue, groupField);
    		for (String rowName : rowNames) {
    			HourSummaryRow row = summary.getOrCreateRow(rowName);
    			row.incEstimate(estimate);
    			row.incSpent(spent);
    			row.incRemaining(remaining);
    		}
    		summary.incTotalEstimate(estimate);
    		summary.incTotalSpent(spent);
    		summary.incTotalRemaining(remaining);
    	}
    	
    	TimeStringGenerator gen = new TimeStringGenerator(hoursPerDay, daysPerWeek);
    	summary.doTotalEstimateString(gen.getTimeString(summary.getTotalEstimate()));
    	summary.doTotalSpentString(gen.getTimeString(summary.getTotalSpent()));
    	summary.doTotalRemainingString(gen.getTimeString(summary.getTotalRemaining()));
    	
    	for (HourSummaryRow row : summary.getRows()) {
    		row.doEstimateString(gen.getTimeString(row.getEstimate()));
    		row.doSpentString(gen.getTimeString(row.getSpent()));
    		row.doRemainingString(gen.getTimeString(row.getRemaining()));
    	}
    	
    	if (sortBy.equals(REMAINING_ESTIMATE)) {
    		Collections.sort(summary.getRows());
    	}
    	if (sortDirection.equals(DESCENDING)) {
    		Collections.reverse(summary.getRows());
    	}
    	
    	return Response.ok(summary).cacheControl(NO_CACHE).build();
    }
    
    private long getRemaining(Issue issue) {
    	try {
    		long out = issue.getEstimate();
    		return out;
    	}catch(Exception e) {
    		return 0;
    	}
    }
    
    private long getEstimate(Issue issue) {
    	try {
    		long out = issue.getOriginalEstimate();
    		return out;
    	}catch(Exception e) {
    		return 0;
    	}
    }
    
    private long getSpent(Issue issue) {
    	try {
    		long out = issue.getTimeSpent();
    		return out;
    	}catch(Exception e) {
    		return 0;
    	}
    }
    
    private List<Issue> getApplicableIssues(String searchId) {
    	User remoteuser = authenticationContext.getUser();
    	
    	if (searchId.contains(FILTER)) {
    		Long filterId = Long.valueOf(searchId.substring(FILTER.length()));
        	
        	SearchRequest srq = searchRequestService.getFilter(new JiraServiceContextImpl(remoteuser), filterId);
        	try {
        		SearchResults filterIssues = searchProvider.search(srq.getQuery(), remoteuser, PagerFilter.getUnlimitedFilter());
        		return filterIssues.getIssues();
        	}catch (Exception e) {
        		return new ArrayList<Issue>();
        	}
    	}
    	else if (searchId.contains(PROJECT)) {
    		Long projectId = Long.valueOf(searchId.substring(PROJECT.length()));
    		
    		try {
    			Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
    			ArrayList<Issue> issues = new ArrayList<Issue>();
    			for (Long issueId : issueIds) {
    				issues.add(issueManager.getIssueObject(issueId));
    			}
    			
    			return issues;
    		}catch (Exception e) {
    			return new ArrayList<Issue>();
    		}
    	}
    	else {
    		return new ArrayList<Issue>();
    	}
    }
    
    private String getSearchDisplayName(String searchId) {
    	User remoteuser = authenticationContext.getUser();
    	
    	if (searchId.contains(FILTER)) {
    		Long filterId = Long.valueOf(searchId.substring(FILTER.length()));
    		
    		SearchRequest srq = searchRequestService.getFilter(new JiraServiceContextImpl(remoteuser), filterId);
    		return srq.getName();
    	}
    	else if (searchId.contains(PROJECT)) {
    		Long projectId = Long.valueOf(searchId.substring(PROJECT.length()));
    		
    		Project proj = projectManager.getProjectObj(projectId);
    		return proj.getName();
    	}
    	return "";
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
    		out.add(issue.getAssignee().getFullName());
    	}
    	else if (nameType.equals(FIX_VERSION)) {
    		Collection<Version> fixVersions = issue.getFixVersions();
    		for (Version fixVersion : fixVersions) {
    			out.add(fixVersion.getName());
    		}
    	}
    	else if (nameType.equals(ISSUE_TYPE)) {
    		out.add(issue.getIssueTypeObject().getName());
    	}
    	else if (nameType.equals(AFFECTED_VERSION)) {
    		Collection<Version> affectedVersions = issue.getAffectedVersions();
    		for (Version affectedVersion : affectedVersions) {
    			out.add(affectedVersion.getName());
    		}
    	}
    	else if (nameType.equals(STATUS)) {
    		out.add(issue.getStatusObject().getName());
    	}
    	else if (nameType.equals(PRIORITY)) {
    		Priority po = issue.getPriorityObject();
    		if (po != null && po.getName() != null) {
    			out.add(po.getName());
    		}
    	}
    	
    	if (out.size() == 0) {
    		out.add("None");
    	}
    	
    	return out;
    }
    ///CLOVER:OFF
    
    public class TimeStringGenerator {
    	private long secondsPerMinute = 60;
    	private long minutesPerHour = 60;
    	private long hoursPerDay;
    	private long daysPerWeek;
    	
    	private long secondsPerHour;
    	private long secondsPerDay;
    	private long secondsPerWeek;
    	
    	public TimeStringGenerator (long hoursPerDay, long daysPerWeek) {
    		this.hoursPerDay = hoursPerDay;
    		this.daysPerWeek = daysPerWeek;
    		
    		secondsPerHour = secondsPerMinute * minutesPerHour;
    		secondsPerDay = secondsPerHour * this.hoursPerDay;
    		secondsPerWeek = secondsPerDay * this.daysPerWeek;
    	}
    	
    	public String getTimeString(long totalSeconds) {
    		long seconds, minutes, hours, days, weeks;
    		long secondsLeft = totalSeconds;
    		String out = "";
    		
    		weeks = secondsLeft / secondsPerWeek;
    		secondsLeft -= weeks * secondsPerWeek;
    		
    		days = secondsLeft / secondsPerDay;
    		secondsLeft -= days * secondsPerDay;
    		
    		hours = secondsLeft / secondsPerHour;
    		secondsLeft -= hours * secondsPerHour;
    		
    		minutes = secondsLeft / secondsPerMinute;
    		secondsLeft -= minutes * secondsPerMinute;
    		
    		seconds = secondsLeft;
    		
    		if (weeks > 0) {
    			out += weeks + "w";
    		}
    		
    		if (days > 0) {
    			if (out.length() != 0) {
    				out += " ";
    			}
    			out += days + "d";
    		}
    		
			if (out.length() != 0) {
				out += " ";
			}
    		out += hours + "h";
    		
    		if (minutes > 0) {
    			out += " " + minutes + "m";
    		}
    		
    		if (seconds > 0) {
    			out += " " + seconds + "s";
    		}
    		
    		return out;
    	}
    }
    
    @XmlRootElement
    public static class HourSummaryResponse {
    	@XmlElement
    	private ArrayList<HourSummaryRow> rows;
    	
    	@XmlElement
    	private String totalEstimateString;
    	
    	@XmlElement
    	private String totalSpentString;
    	
    	@XmlElement
    	private String totalRemainingString;
    	
    	@XmlElement
    	private String searchName;
    	
    	private long totalEstimate;
    	private long totalSpent;
    	private long totalRemaining;
    	
    	//private HourSummaryResponse() {}
    	
    	HourSummaryResponse() {
    		this.rows = new ArrayList<HourSummaryRow>();
    		this.totalEstimate = 0;
    		this.totalSpent = 0;
    		this.totalRemaining = 0;
    	}
    	
    	public ArrayList<HourSummaryRow> getRows() {
    		return rows;
    	}
    	
    	public long getTotalEstimate() {
    		return totalEstimate;
    	}
    	
    	public long getTotalSpent() {
    		return totalSpent;
    	}
    	
    	public long getTotalRemaining() {
    		return totalRemaining;
    	}
    	
    	public void incTotalEstimate(long inc) {
    		totalEstimate += inc;
    	}
    	
    	public void incTotalSpent(long inc) {
    		totalSpent += inc;
    	}
    	
    	public void incTotalRemaining(long inc) {
    		totalRemaining += inc;
    	}
    	
    	public void doSearchName(String searchName) {
    		this.searchName = searchName;
    	}
    	
    	public void doTotalEstimateString(String value) {
    		totalEstimateString = value;
    	}
    	
    	public void doTotalSpentString(String value) {
    		totalSpentString = value;
    	}
    	
    	public void doTotalRemainingString(String value) {
    		totalRemainingString = value;
    	}
    	
    	public HourSummaryRow getOrCreateRow(String rowName) {
        	HourSummaryRow temp = new HourSummaryRow(rowName);
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
    public static class HourSummaryRow implements Comparable<HourSummaryRow>{
    	@XmlElement
    	private String name;
    	
    	@XmlElement
    	private String estimateString;
    	
    	@XmlElement
    	private String spentString;
    	
    	@XmlElement
    	private String remainingString;
    	
    	private long estimate;
    	private long spent;
    	private long remaining;
    	
    	private HourSummaryRow() {}
    	
    	public HourSummaryRow(String name) {
    		this.name = name;
    		this.estimate = 0;
    		this.spent = 0;
    		this.remaining = 0;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public long getEstimate() {
    		return estimate;
    	}
    	
    	public void incEstimate(long inc) {
    		estimate += inc;
    	}
    	
    	public long getSpent() {
    		return spent;
    	}
    	
    	public void incSpent(long inc) {
    		spent += inc;
    	}
    	
    	public long getRemaining() {
    		return remaining;
    	}
    	
    	public void incRemaining(long inc) {
    		remaining += inc;
    	}
    	
    	public void doEstimateString(String value) {
    		estimateString = value;
    	}
    	
    	public void doSpentString(String value) {
    		spentString = value;
    	}
    	
    	public void doRemainingString(String value) {
    		remainingString = value;
    	}
    	
    	public int compareTo(HourSummaryRow o) {
    		return (int) ((this.getRemaining()) - (o.getRemaining()));
    	}
    	
    	public boolean equals(Object o) {
    		if (o instanceof HourSummaryRow) {
    			return ((HourSummaryRow) o).getName().equals(this.getName());
    		}
    		return false;
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