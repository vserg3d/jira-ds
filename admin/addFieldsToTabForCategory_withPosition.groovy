import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.*
import org.apache.log4j.Level
import org.apache.log4j.Logger
import utils.Fields
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.FieldManager
import com.atlassian.jira.issue.fields.OrderableField

log = Logger.getLogger("com.vlas.addFieldToScreen")
log.setLevel(Level.DEBUG)
def schemeManager = ComponentAccessor.issueTypeScreenSchemeManager
def cfManager = ComponentAccessor.getCustomFieldManager()
FieldManager fieldManager = ComponentAccessor.getFieldManager()

//replace with your issue types names
def issueTypesInclude =[]  //["Epic","Bug","Improvement","Task","Query","Regulatory Update","Sub-Bug","Bug","Task"]
def issueTypesExclude =[] //["Test","Sub-Bug","Sub-Defect","Sub-Style","Sub-Task","Defect","Sub-Testing"]

//replace with your categories
def List categories = [""]//["Platform", "Scripts"]

def screenschemesFound =[]//will store the found screen schemes
Set issuetypes =[]//will store found issue types for projects
Set<FieldScreen>  screensFound = []//will store the found screens
def html = ""// output
def fieldsToInsert = ['FIELD NAME']  // the fields which will be added to screens  "Field1", Field2"

// field after which to insert new field(s),can be custom field or system (assignee, description, etc.). If empty position will be set to last
def fieldWithPositionName=  "FIELD NAME"
def fieldWithPositionId = ''

if (fieldWithPositionName) {
    // check that there is such id for system field
    fieldWithPositionId = fieldManager.getOrderableField(fieldWithPositionName)?.getId()
    // if no Id get id for Custom Field
    if (!fieldWithPositionId) fieldWithPositionId = cfManager.getCustomFieldObjectsByName(fieldWithPositionName).getAt(0)?.id
    assert fieldWithPositionId: "there is no field with name: $fieldWithPositionName"
}
// tab where to insert field
def tabName = ''//"Notes"
// for testing
// def project = ComponentAccessor.getProjectManager().getProjectByCurrentKey('PROJect key') //
// def issueTypes = ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(project) //"Epic"

categories.each{ category ->
    def categoryId = ComponentAccessor.getProjectManager().getProjectCategoryObjectByNameIgnoreCase(category).getId()
//iterate over all projects in category
    html +=  "<h1>Category:$category</h1>"
    ComponentAccessor.getProjectManager().getProjectObjectsFromProjectCategory(categoryId).each{ project->
    html +=  "${project.getName()} (${project.getKey()}), "
     //iterate over all issue types for this project
        project.getIssueTypes().each{ issuetype->
            def thisIssueTypeName = issuetype.name.toString()
            //check issue type: if issueTypesInclude is Empty or type is listed in issueTypesInclude and type is not listed in issueTypesEXclude
            if((!issueTypesInclude || issueTypesInclude.contains(thisIssueTypeName)) && !issueTypesExclude.contains(thisIssueTypeName) ) {
                //get issue type screen scheme name for project
                def isss = schemeManager.getIssueTypeScreenScheme(project)
                def isssEntities = isss.getEntities()
                def isssueTypeId = issuetype.getId()
                issuetypes.add(issuetype.getName())
                //iterate over issue type screen scheme entities
                def found = false
                isssEntities.each{ entity->
                    //if issuetype is used, add to results
                    if((entity.getIssueTypeId() == isssueTypeId)){
                        def fieldScreenScheme =entity.getFieldScreenScheme()
                        def schemeName = fieldScreenScheme.getName()
                        screenschemesFound.add(schemeName)
                        def schemeItems = fieldScreenScheme.getFieldScreenSchemeItems()
                        schemeItems.each{
                            screensFound.add(it.getFieldScreen() )
                        }
                        found = true
                    }
                }
                if (!found){
                    //if no screen scheme found for the issue type, it must be the default
                    isssEntities.each{ entity->
                        if(!entity.getIssueTypeId()){
                            def fieldScreenScheme =entity.getFieldScreenScheme()
                            def schemeName = fieldScreenScheme.getName()
                            screenschemesFound.add(schemeName)
                            def schemeItems = fieldScreenScheme.getFieldScreenSchemeItems()
                            schemeItems.each{
                                screensFound.add(it.getFieldScreen() )
                            }
                        }
                    }
                }
            }
        }
    }
}
html += "<h4>Screens for issuetypes:${issuetypes.sort()} of included issue types: $issueTypesInclude and excluded issue types: $issueTypesExclude for categories $categories</h4>"

(screensFound.unique().sort{it.name}).each{ FieldScreen fieldScreen ->
    html += "<h3> ${fieldScreen.getName()}, id:${fieldScreen.getId()}</h3>"
    def tabs = fieldScreen.getTabs()
    FieldScreenTab tabForInsert = null
    if (tabName) {
        tabForInsert = tabs.find{it.name==tabName}
        if (!tabForInsert) {
        //  tabForInsert = fieldScreen.addTab(tabName) // adding absent tab
            return"<h3>!!! the Tab:'${tabName}' is absent</h3>"
        }
    //if field after which to insert is set find tab with such field, otherwise add to first tab
    } else if(fieldWithPositionId && fieldScreen.containsField(fieldWithPositionId)){
        tabForInsert = tabs.find{it.isContainsField(fieldWithPositionId)}
    } else {
        tabForInsert =tabs.getAt(0) // if tab was not set add the field to the first tab
    }
    fieldsToInsert.each { fieldToInsert ->
        // position where to add fields. -1 means the last. 0 - first
        def position = -1
        // if the field after which to insert is set position will be overwriten
        if (fieldWithPositionId && tabForInsert.isContainsField(fieldWithPositionId)) {
            position = tabForInsert.getFieldScreenLayoutItem(fieldWithPositionId).getPosition()+1 //insert after
        }
        //addFieldToScreen(String fieldName, String screenName, String tabName='', Integer position=-1, Boolean isDebugMode=false)
        //  log.debug('screen:'+fieldScreen.getName()+'. position:'+position)
        html += Fields.addFieldToScreen(fieldToInsert,fieldScreen.getName(),tabForInsert.name,position,true)+'<br>'
   }
}
html += "<hr><h2>Screen Scheems</h2>"
//output unique list of screenschemes
(screenschemesFound.unique()).each{
    html += it.toString() + "<br>"
}
return html
