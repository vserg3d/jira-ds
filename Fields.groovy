// file name: Fields
// short description: class for Fields updates

// methods: setHighestOption


package utils
import com.atlassian.jira.issue.fields.screen.FieldScreenTab
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.Issue
import utils.Common
import utils.Constants
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import org.apache.log4j.Level
import org.apache.log4j.Logger
import com.atlassian.jira.issue.fields.FieldManager
import com.atlassian.jira.issue.fields.OrderableField

class Fields {

    // add field to screen, tab, position
    // if isDebugMode=true function will generate message without real adding
    // example of usage from console when we have list of screens,tabs,positions for insert:
        /*
        import java.lang.String
        import java.lang.Integer
        import utils.Fields
        def screens =[   //array of arrays (screen, tab, position)
                        ['[Test] screen', '',-1],
                        ['Screen1', 'Main Tab',4]
        ]
        def fieldName = 'FIELD NAME' //Description, description,assignee
        def html =''
        screens.each { arr ->
        String screenName  =arr[0] as String
        String tabName= arr[1]  as String
        def position=arr[2] as Integer
        //addFieldToScreen(String fieldName, String screenName, String tabName='', Integer position=-1, Boolean isDebugMode=false)
        html +=Fields.addFieldToScreen(fieldName,screenName,tabName,position,false)+"<br>"
        }
        return html
        */
    public static String addFieldToScreen(String fieldName, String screenName, String tabName='', Integer position=-1, Boolean isDebugMode=false) {
        String result = "addFieldToScreen started. "
        def customFieldManager = ComponentAccessor.getCustomFieldManager()
        def optionsManager = ComponentAccessor.getOptionsManager()
        FieldManager fieldManager = ComponentAccessor.getFieldManager()
        // get system field
        def field = fieldManager.getOrderableField(fieldName)
        // if there is no system field, search for Custom field
        if (!field) field = customFieldManager.getCustomFieldObjectsByName(fieldName).getAt(0)
        if (!field) return "No such field $fieldName"

        FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
        Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens(screenName)
        if (fieldScreens.size() !=1 ) return "Not 1 screen for $screenName . Founded screens: ${fieldScreens*.getName()}"
        FieldScreenTab tab = null
        FieldScreen fieldScreen = fieldScreens[0]
        if (tabName) tab =fieldScreen.getTabs().findByName(tabName) else tab=fieldScreen.getTab(0)
        if (!tab) return "No such tab: $tabName for screen: $screenName"

        if (!fieldScreen.containsField(field.id)) {
            // if debug mode the field will not be really inserted, just return potenrial message
            if (!isDebugMode) {
                if (position==-1) { // if position is not set add to the end (last position)
                    tab.addFieldScreenLayoutItem(field.id)
                } else {
                    tab.addFieldScreenLayoutItem(field.id,position)
                }
            }
            result += "Field '${field.name}' inserted at position:'$position' "
        }
        else {
            result +="Screen already has field '${field.name}'. "
        }
        result += "screen: '${fieldScreen.getName()}', tab: '${tab.getName()}'."

        return result
    }

    // add option to select list
    public static String addvalue(String fieldName, String insertValue, Integer position=0) {
        String result = "Nothing changed."
        def customFieldManager = ComponentAccessor.getCustomFieldManager()
        def optionsManager = ComponentAccessor.getOptionsManager()
        def cVField = customFieldManager.getCustomFieldObjectsByName(fieldName).getAt(0)

        def cVFieldConfig = cVField.getRelevantConfig(com.atlassian.jira.issue.context.IssueContext.GLOBAL)
        def options = optionsManager.getOptions(cVFieldConfig)
        if(!options.any{it.value == insertValue} ){
            def newOption =optionsManager.createOption(cVFieldConfig,null,0,insertValue)
            options.moveOptionToPosition( [(position):newOption])
            result ="Field: $fieldName Value: $insertValue was inserted. "
        }
        return result
    }

    //the field "FindHighestOption" is set as the "highest" value from the linked  Jira Projects
    public static def setHighestOption(MutableIssue sourceIssue) {
        def log = Logger.getLogger("com.vlassl.jiraautomation.fileds.setHighestOption")
        log.setLevel(Level.DEBUG)
        def fieldName = "FindHighestOption"
        def projectCagerories = Constants.SEARCHABLEPROJECTS
        def fieldWithOptions = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldName)
        def issue = sourceIssue as MutableIssue
        log.debug("source Issue for SHO set: ${issue.getKey()}")
        def issueProject = issue.getProjectObject()
        def issuyType =issue.getIssueType().getName()
        ArrayList<Issue> linkedSearchableIssues = Common.getIssueLinks(issue,projectCagerories)
        def highestValue = null
        //if (!linkedSearchableIssues) { return 'No links'}
        if (linkedSearchableIssues) {
            def searchableIssuesSHO = linkedSearchableIssues*.getCustomFieldValue(fieldWithOptions.getAt(0)).findAll()
            log.debug("searchableIssuesSHO: ${searchableIssuesSHO}")
            //log.debug("searchableIssuesSHO has:" +searchableIssuesSHO.contains("User Error"))
            //if (!searchableIssuesSHO) { return 'No fields'}
            if (searchableIssuesSHO) {
                def customFieldManager = ComponentAccessor.getCustomFieldManager()
                def optionsManager = ComponentAccessor.getOptionsManager()
                def OptionsField = customFieldManager.getCustomFieldObjectsByName(fieldName).getAt(0)
                def fieldConfig = OptionsField.getRelevantConfig(com.atlassian.jira.issue.context.IssueContext.GLOBAL)
                def allOptions = ComponentAccessor.optionsManager.getOptions(fieldConfig)
                def currentOptions = allOptions.findAll{searchableIssuesSHO.contains(it)}
                // return "currentOption: "+currentOptions.sort{ it.sequence}[0]?.value
                log.debug("currentOptions: $currentOptions . The highest:${currentOptions.sort{it.sequence}[0]}")
                //def option = optionsManager.getOptions(fieldConfig).find {it.value == currentOptions.sort{ it.sequence}[0]?.value}
                if (currentOptions) {  // if there are any linked Searchable issues with not empty SHO select the highest SHO for issue
                    highestValue = currentOptions.sort{ it.sequence}[0]
                }
            }
        }
        def issueManager = ComponentAccessor.getIssueManager()
        //def issue = issueManager.getIssueObject("TEST-17")
        def user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
        // set the highest SHO or null (if there is no links or values)
        issue.setCustomFieldValue(fieldWithOptions.getAt(0),highestValue)
        issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, true)
    }
}

// usage
// import utils.Fields
// Fields.EXAMPLESTATUS