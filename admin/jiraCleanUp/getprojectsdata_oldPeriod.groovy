import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.lang.Integer
import com.atlassian.jira.project.Project
import com.atlassian.jira.component.ComponentAccessor

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.user.ApplicationUser

import com.atlassian.jira.issue.search.SearchException
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.web.bean.PagerFilter
import org.apache.log4j.Level
import org.apache.log4j.Logger
import groovy.io.FileType
import static groovy.io.FileType.*

log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)
def timeStart = new Date()
def html='Starting: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'  // Ukraine time :)

List categoriesExclude = ["Archived","Platform"]
List categoriesInclude= [] //  - for testing only

List infoList=["projectkey,countIssues,lastUpdated,attacmentsSize before 2023"]
def csvFile = new File('/opt/jira.home/scripts/temp','projects_csv.groovy')
Integer row=1
String getProjectDataFromJQL(String project='') {
    if (!project) return
    String result="$project"
    // delete ORDER BY section if exists
    String jqlSearch = "Project = $project AND created < startOfYear(-2) AND statusCategory = Done and resolved < startOfYear(-2) ORDER  BY updated"
    ApplicationUser user =ComponentAccessor.userManager.getUserByName('user with privileges')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
    assert user:'please set user name'
    def searchService = ComponentAccessor.getComponentOfType(SearchService)

    // Parse the query
    def parseResult = searchService.parseQuery(user, jqlSearch)
    if (!parseResult.valid) {
        log.error('Invalid query')
        return null
    }
    try {
        def issueCount = searchService.searchCount(user, parseResult.query) as Integer
        result +=",${issueCount.toString()}"
        // Perform the query to get all issues for project
        def listOfIssues = searchService.search(user, parseResult.query,  PagerFilter.unlimitedFilter).results
        String updated = 'null'
        def lastIssue = listOfIssues[0] // get first (last updated) issue
        if (lastIssue) {
            updated = lastIssue.updated.format("yyyy-MM-dd") // HH:mm:ss
           // log.info("LAst updated issue: ${lastIssue.key} , ${updated}")
        }
        result +=",${updated}"
        Long attahmemntsSize = 0
        listOfIssues.each{
          // log.debug('key:' +it.key +' size:'+it.getAttachments()*.filesize.sum())
           def attahmemntsSizeIssue = it.getAttachments()*.filesize.sum()
           if (attahmemntsSizeIssue) attahmemntsSize += attahmemntsSizeIssue as Long
        }
        result +=",${attahmemntsSize}"
        log.debug(result)
    } catch (SearchException e) {
        e.printStackTrace()
        null
    }
    return result
}
def projectManager = ComponentAccessor.getProjectManager()
def projectsAll = projectManager.getProjects()
def projects = projectsAll.findAll{ project -> !categoriesExclude.contains(project.projectCategory?.getName())} //categoriesInclude //!categoriesExclude.contains(project.projectCategory?.getName())
projects.each { project ->
    def key = project.getKey()
    row++
    infoList << getProjectDataFromJQL(key)
}
csvFile.withWriter { out ->
  infoList.each {
    out.println it
  }
}
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
html +="Rows: $row <br>Duration: $duration<br/>"
return html