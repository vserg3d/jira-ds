import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.lang.Integer
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.customfields.option.Option
import org.apache.log4j.Level
import org.apache.log4j.Logger
import utils.Common
def im = ComponentAccessor.issueManager
ApplicationUser currentUser =ComponentAccessor.userManager.getUserByName('jira_superuser')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
Integer movingValuesCount =0
log = Logger.getLogger("com.vlas.copyvalues")
log.setLevel(Level.DEBUG)
def timeStart = new Date()
def html='Starting: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'  // Ukraine time :)
//Field,which values will be copied
final cFieldFromName =  "FieldFromName" //"Field From"  , "Description of Requirement"
final cFieldToName =  "FieldCopyToName" //
// Field which will store moved values. Not used. Copying will be done to Description system field
//final fieldToName = "Field To"
def customFieldFrom = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(cFieldFromName).getAt(0) //check that only one field with such name?
def customFieldTo = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(cFieldToName).getAt(0) //check that only one field with such name?
//get issues
def JQL =""  //issuekey =NAPD-133 "'Current Developer' is not EMPTY and Developer is EMPTY"
// //log.debug(JQL)
def issues= Common.getIssuesFromJQL(JQL)
movingValuesCount =issues.size()
log.debug( issues.size() )
issues.each { issue ->
    def issueEditable = im.getIssueObject(issue.key)
    //next code can be used to copy to system field (Description as an example)
    /*
    def curentDescription = ''
    log.debug( issue.getDescription() )
    if (issue.getDescription()) {
        curentDescription = issue.getDescription().toString()+"\n\n_Value copied from '$cFieldFromName':_\n"
    }
    log.debug(issueEditable.key)
    //change the issue object (in memory only)
    issueEditable.setDescription(curentDescription+issue.getCustomFieldValue(customField).toString())
    */
    //code used to copy to custom field
    //change the issue object (in memory only)
    issueEditable.setCustomFieldValue(customFieldTo,issue.getCustomFieldValue(customFieldFrom))
    //this saves the changed issue to the db
    im.updateIssue(currentUser,issueEditable, EventDispatchOption.DO_NOT_DISPATCH,false) //
    // DO_NOT_DISPATCH saves a lot of execution time, but reindex is required (built-in script can be used)
}

log.debug('Total:'+movingValuesCount)
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
html += "<br>Duration: $duration<br/>Total:$movingValuesCount <br/>"
html +='Finished: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'
return html