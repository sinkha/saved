These are back-end Jira management scripts...

How to use these scripts:

1.) Create a filter with the issues that need to be changed.
2.) Identify the filterId. Note: you can usually find this in
	the URL while viewing the filter.
3.) Update the filterId in the script.
4.) Go to "Administration"->"Script Runner"
5.) Select the "Groovy" option for the "Script Engine:".
6.) Copy and paste script into "Script:" field.
7.) Click "Run now".
Note: All scripts should be tested on the testjira server before
being run on production Jira.

BulkChangeEstimatedTimeRemainingScript.groovy
	Takes a filter (filterId must be set in the script before use) 
	and sets the time estimates to 8hrs for each issue in the filter.
	
BulkChangeSummaryScript.groovy
	Takes a filter (filterId must be set in the script before use) 
	and removes "Clone - " from the summary field.

FixSummaryScript.groovy
	Takes a filter (filterId must be set in the script before use) 
	which has issues that are imported with the incorrect summary
	and rearranges the order of the summary. Usually, this script
	has to be tweaked before use. The summary is usually made up of
	2 or more section separated by ':' that need to be reordered. 
	