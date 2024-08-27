package utils
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.issue.search.SearchException
import com.atlassian.jira.web.bean.PagerFilter




class Common {

    public static final String EXAMPLESTATUS = "Closed"


    public static ArrayList<Issue> getIssueLinksForPrtojects(Issue sourceIssue, List linkedProjects) {
      ArrayList<Issue> listLinkedIssues = []
      def log = Logger.getLogger("com.jiraautomation.common.getissuelinks")
      log.setLevel(Level.DEBUG)
      List<IssueLink> outIssueLinks = ComponentAccessor.getIssueLinkManager().getOutwardLinks(sourceIssue.getId())
      listLinkedIssues.addAll(outIssueLinks*.getDestinationObject().findAll {linkedProjects.contains(it.getProjectObject().getKey())})
      List<IssueLink> inIssueLinks = ComponentAccessor.getIssueLinkManager().getInwardLinks(sourceIssue.getId())
      listLinkedIssues.addAll(inIssueLinks*.getSourceObject().findAll {linkedProjects.contains(it.getProjectObject().getKey())})
      log.debug("LINKED ISSUES:: Projects:" + linkedProjects + "ISSUES: " + listLinkedIssues*.getKey())
      return listLinkedIssues
    }

    public static ArrayList<Issue> getIssueLinks(Issue sourceIssue, List linkedProjectCategory=[]) {
      ArrayList<Issue> listLinkedIssues = []
      def log = Logger.getLogger("com.jiraautomation.common.getissuelinks")
      log.setLevel(Level.DEBUG)
      def projectManager = ComponentAccessor.getProjectManager()
      List<IssueLink> outIssueLinks = ComponentAccessor.getIssueLinkManager().getOutwardLinks(sourceIssue.getId())
      listLinkedIssues.addAll(outIssueLinks*.getDestinationObject().findAll {linkedProjectCategory==[] || linkedProjectCategory.contains(
                      projectManager.getProjectCategoryForProject(it.getProjectObject())?.getName())})


      List<IssueLink> inIssueLinks = ComponentAccessor.getIssueLinkManager().getInwardLinks(sourceIssue.getId())
      listLinkedIssues.addAll(inIssueLinks*.getSourceObject().findAll {linkedProjectCategory==[] || linkedProjectCategory.contains(
                      projectManager.getProjectCategoryForProject(it.getProjectObject())?.getName())})

      log.debug("LINKED ISSUES:: CATEGORIES:" + linkedProjectCategory + "ISSUES: " + listLinkedIssues*.getKey())

      return listLinkedIssues
    }


// resultsCount limits number of returned issues !! If all results (not recomended as high load for Jira) are required you can use  Integer.MAX_VALUE
    public static List<Issue> getIssuesFromJQL(String jqlSearch='', Integer resultsCount=Integer.MAX_VALUE) {// The JQL query you want to search with
      def issues = null
      def log = Logger.getLogger("com.jiraautomation.common.getissuesfromjql")
      log.setLevel(Level.INFO)
      log.info("jqlSearch: $jqlSearch")
      if (!jqlSearch) return
      def user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
      def searchService = ComponentAccessor.getComponentOfType(SearchService)

      // Parse the query
      def parseResult = searchService.parseQuery(user, jqlSearch)
      if (!parseResult.valid) {
          log.error('Invalid query')
          return null
      }
      try {
          // Perform the query to get the issues  // user.city ?: user.state
          def pf = new PagerFilter(resultsCount)
          def results = searchService.search(user, parseResult.query, pf)
          issues = results.results
          if (issues) { log.debug("Issues from JQL: "+issues*.key) }

      } catch (SearchException e) {
          e.printStackTrace()
          null
      }
      return issues
    }
}

// usage
// import utils.Common
// Common.EXAMPLESTATUS

