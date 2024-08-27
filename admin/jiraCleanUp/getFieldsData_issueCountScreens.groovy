import groovy.time.TimeDuration
import com.atlassian.jira.component.ComponentAccessor
import utils.Common
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenTab
// import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager
// import com.atlassian.jira.issue.fields.screen.FieldScreenScheme
// import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager
// import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme
import java.lang.Integer
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.issue.search.SearchException
import org.apache.commons.text.StringEscapeUtils
import groovy.time.*
import org.apache.log4j.Level
import org.apache.log4j.Logger

def log = Logger.getLogger("com.jiraautomation.common")
log.setLevel(Level.DEBUG)

List infoList=["Name,Key,Type,IssuesCount,Contexts,ScreensCount,Screens,Description"] //,IssuesCount,Projects
Integer row=0
def issueFile = new File('/opt/jira.home/scripts/temp','fields_screens_csv.groovy')
String str=""

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def fieldsList = customFieldManager.getCustomFieldObjects() //.subList(0,285)  // !!! subList for testing only
// for testing
//fieldsList = customFieldManager.getCustomFieldObjects().findAll(){fields.contains(it.name.toUpperCase())}
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
ApplicationUser user =ComponentAccessor.userManager.getUserByName('jira_superuser')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
def searchService = ComponentAccessor.getComponentOfType(SearchService)
def timeStart = new Date()
def html='Starting: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'  // Ukraine time :)

fieldsList.each() { field ->
  // if issue count, projects are required //1
    def issueCount = 0
    def screens =''
    def screensCounter =0
    // added configurations data (not tested :^) ). If only default configuration is used with no context (contexts=0) the field is not used by any project
    def contexts =0
    field.getConfigurationSchemes().each{ scheme ->
        contexts +=scheme.getContexts().size()
        if (scheme.	isAllProjects() ) {contexts +=299}// context wil be 300 if context is Global (to simplify the logic)
    }
    fieldScreens.each { fieldScreen ->
      List<FieldScreenTab> tabs = fieldScreen.getTabs()
      tabs.each { tab ->
        if (tab.isContainsField(field.id)) {
          screens += fieldScreen.name+';'
          screensCounter++
        }
      }
    }

    def jqlStr= "cf[${field.getId().substring(12)}] is not EMPTY"
    def parseResult = searchService.parseQuery(user, jqlStr)
    log.debug(parseResult.query)
    if (!parseResult.valid) {
        log.error('Invalid query')
        return null
    }
    try {
        issueCount = searchService.searchCount(user, parseResult.query) as Integer
    } catch (Exception e) {
        log.error(e.toString())
        issueCount =-1
    }
    screens =StringEscapeUtils.escapeCsv(screens)
// if issue count, projects are required //2
    str="${StringEscapeUtils.escapeCsv(field.getName())},${field.getId() },${field.getCustomFieldType().getName() },$issueCount,$contexts, $screensCounter,$screens,${StringEscapeUtils.escapeCsv(field.getDescription())}"
    html += str+"<br>"
    infoList << str

}
// return html
issueFile.withWriter { out ->
  infoList.each {
    out.println it
  }
}
// dutation in seconds
//(end.getTime()-start.getTime())/1000
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
html +="Duration: $duration<br/>"
return html

/*
----------------
import com.atlassian.jira.component.ComponentAccessor
import utils.Common
def fields= ['BILLABLE']
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def fieldsList = customFieldManager.getCustomFieldObjects().findAll(){fields.contains(it.name.toUpperCase())}
def html=''
fieldsList.each() {
    def jqlStr= "cf[${it.getId().substring(12)}] is not EMPTY"
    def jqlResult = Common.getIssuesFromJQL(jqlStr)
    html +="${it.getName()}. Key: ${it.getId() }.  Number of issues: ${jqlResult.size()} . Projects: ${jqlResult*.project.name.unique()} <br>"
}
return html
*/
