import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.lang.Integer
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.index.IssueIndexingService
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.customfields.option.Option
import org.apache.log4j.Level
import org.apache.log4j.Logger
import utils.Common
def im = ComponentAccessor.issueManager
ApplicationUser currentUser =ComponentAccessor.userManager.getUserByName('jira_superuser')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser

// remapping structure - array of arrays
// 1st (0 index) and 2nd element of internal array contain old "Cascade"  values (will be disabled)
// 3d and 4th element - new values. Values should exist
// 5th element  (optional) - 3d level (optional additional mapping )
def mapping = [
// ['OLD 1','OLD 2','NEW 1','NEW 2'],
// ['OLD 1','OLD 2','NEW 1','NEW 2','3D LEVEL'],
]
List mappingOld = [] // array of old CASCADE values (will be disabled)
mapping.each { mappingOld.add(it.subList(0,2)) }
// //def list2=list.
mappingOld =mappingOld.unique()
Integer issuesForMappingCount =0
log = Logger.getLogger("com.vlas.updateissuecascade")
log.setLevel(Level.DEBUG)
def JQLList = []
def JQLNewList = []
def timeStart = new Date()
def html='Starting: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'  // Ukraine time :)
final customFieldName = "cascade field"  //
def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(customFieldName)
def optionsManager = ComponentAccessor.getOptionsManager()
// for local scope (issue) should be used instead of (com.atlassian.jira.issue.context.IssueContext.GLOBAL)// if scope is not global get issue where field exists and use it
FieldConfig fieldConfig = customField.getRelevantConfig(com.atlassian.jira.issue.context.IssueContext.GLOBAL)
Options options = optionsManager.getOptions(fieldConfig)
//ArrayList<ArrayList>
def setDisabled(List list, Options options) {
    list.each {
        def mappingArray =it as String[]
        Option parentOptionOld = options.getOptionForValue(mappingArray[0], null) //(String optionName, long parentOptionId), so this is for parent
        assert parentOptionOld:"parent option ${mappingArray[0]} is absent"
        Option childOptionOld = options.getOptionForValue(mappingArray[1], parentOptionOld.getOptionId()) //and this for child
        assert childOptionOld:"child option ${mappingArray[1]} is absent"
        childOptionOld.setDisabled(true)
    }
}

setDisabled(mappingOld,options)  //!!! setting the value as disabled !!!
//return

mapping.each { values ->
    def mappingArray =values as String[]
    Option parentOptionOld = options.getOptionForValue(mappingArray[0], null) //old Product Group
    assert parentOptionOld:"parent option ${mappingArray[0]} is absent"
    Option childOptionOld = options.getOptionForValue(mappingArray[1], parentOptionOld.getOptionId()) //old Product Category
    assert childOptionOld:"child option ${mappingArray[1]} is absent"
    Option parentOptionToSet = options.getOptionForValue(mappingArray[2], null) //new Prodcut Group
    assert parentOptionToSet:"parent option ${mappingArray[2]} is absent"
    Option childOptionToSet = options.getOptionForValue(mappingArray[3], parentOptionToSet.getOptionId()) //new Product Category
    assert childOptionToSet:"child option ${mappingArray[3]} is absent"
    //log.debug(mappingArray.toString())
    Map<Object, Option> optionsToSet = new HashMap<Object, Option>() {
        {
            put(null, parentOptionToSet)
            put("1", childOptionToSet)
        }
    }
    // //def issue = im.getIssueObject("TEST-111") //for testing
    //get issues
    def JQL ="\"$customFieldName\" in cascadeOption(\""+parentOptionOld.getValue()+'","'+childOptionOld.toString()+'")'
    if (mappingArray.size()==5) JQL +=" AND \"3d level cascade\" =\"${mappingArray[4]}\"  "
    JQLNewList.add("\"$customFieldName\" in cascadeOption(\""+parentOptionToSet.getValue()+'","'+childOptionToSet.toString()+'")')

    JQLList.add(JQL)
    // //log.debug(JQL)
    def issues= Common.getIssuesFromJQL(JQL)
    issuesForMappingCount +=issues.size()
    log.debug( issues.size() )
    ArrayList<Issue>  issueEdited = []
    issues.each { issue ->
     // MutableIssue issue = it as MutableIssue

        def issueEditable = im.getIssueObject(issue.key)
     //   log.debug(issueEditable.key)
        //change the issue object (in memory only)
        issueEditable.setCustomFieldValue(customField, optionsToSet)
        //this saves the changed issue to the db
        im.updateIssue(currentUser,issueEditable, EventDispatchOption.DO_NOT_DISPATCH,false) //
        // DO_NOT_DISPATCH saves a lot of execution time, but reindex is required (built-in script can be used or logic below)
    //  issueEdited.add(issueEditable)
    }
}
log.debug('Total:'+issuesForMappingCount)
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
html +="JQLList: <br/>${JQLList.join(' OR <br>')} <br/>JQL New list: <br/>${JQLNewList.join(' OR <br>')} <br>Duration: $duration<br/>Total:$issuesForMappingCount <br/>"
html +='Finished: '+timeStart.format('yyyy/MM/dd HH:mm', TimeZone.getTimeZone("GMT+2"))+'<br/>'
return html


// def wasIndexing = ImportUtils.indexIssues
//     ImportUtils.indexIssues = true
//     ComponentAccessor.getComponent(IssueIndexingService).reIndex(issueEditable)
//     ImportUtils.indexIssues = wasIndexing
// def issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
// issueIndexingService.reIndexIssueObjects(issueEdited)
//now we have to re-index the issue, this doesn't happen automatically after update