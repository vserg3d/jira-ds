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
import java.lang.Integer
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.issue.search.SearchException
import org.apache.commons.text.StringEscapeUtils
import groovy.time.*
import org.apache.log4j.Level
import org.apache.log4j.Logger

def log = Logger.getLogger("com.vlassl.jiraautomation.common")
log.setLevel(Level.DEBUG)

def customFieldManager = ComponentAccessor.getCustomFieldManager()
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
def searchService = ComponentAccessor.getComponentOfType(SearchService)

ApplicationUser user =ComponentAccessor.userManager.getUserByName('jira_superuser')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
Integer rows=0
String str=""
def fields= ['field name',]

def timeStart = new Date()
def html='Starting fields deletion: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'+  // Ukraine time :)
         'name, key, type, issuesCount,screensCount,result<br>'
def fieldsList = customFieldManager.getCustomFieldObjects().findAll(){fields.contains(it.name)}
fieldsList.each() { field ->
    def issueCount = 0
    def screensCounter =0
    def result=''
    def contexts =0
    fieldScreens.each { fieldScreen ->
      List<FieldScreenTab> tabs = fieldScreen.getTabs()
      tabs.each { tab ->
        if (tab.isContainsField(field.id)) {
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
    // if issuecount=0 and screenscount=0 the field can be deleted
    if (0==issueCount && 0==screensCounter) {
        customFieldManager.removeCustomField(field)
        result ='deleted'
        rows++
    } else {
      result ='NOT deleted'
    }
    str="${StringEscapeUtils.escapeCsv(field.getName())},${field.getId() },${field.getCustomFieldType().getName() },$issueCount,$screensCounter,$result"
    html += str+"<br>"
}
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
html +="Deleted fields:$rows Duration: $duration<br/>"
return html
