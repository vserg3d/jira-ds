/*
get fields data for selected project category, issue types
stored in csv format
"Project Name,Project Key,Screen,Tab,Field Name,Field Key,Field Type"
*/

import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.*
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.commons.text.StringEscapeUtils

log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)
def schemeManager = ComponentAccessor.issueTypeScreenSchemeManager
//CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
//replace with your issue type names
def issueTypes= ["Bug",
//"Defect",
"Improvement",
"Internal Task",
"Regulatory Update",
"Sub-Query",
"Sub-Task",
"Sub-Testing",
"Task",]

//replace with your categories
def List categories = ["Products"] //["Scripts"]

//will store the found screen schemes
def screenschemesFound =[]
//will store the found issue type screen schemes
def itscreenschemesFound =[]
//will store the found screens
Set<FieldScreen>  screensFound = []
def html = ""
def projectsData =[]
List infoList=["Screen,Tab,Field Name,Field Key,Field Type"]
Integer row=0
def issueFile = new File('/opt/jira.home/scripts/temp','fields_screens_csv.groovy')
//String str=""
categories.each{ category ->
    def categoryId = ComponentAccessor.getProjectManager().getProjectCategoryObjectByNameIgnoreCase(category).getId()
//iterate over all projects in category
    html +=  "<h1>$category</h1>"
    ComponentAccessor.getProjectManager().getProjectObjectsFromProjectCategory(categoryId).each{ project->
   // str =  "${StringEscapeUtils.escapeCsv(project.getName())},${project.key},"
    html +=  "${StringEscapeUtils.escapeCsv(project.getName())},${project.key}; "
     //iterate over all issue types for this project
        project.getIssueTypes().each{ issuetype->
            def thisIssueTypeName = issuetype.name.toString()
            //check if correct issue type
            if(issueTypes.contains(thisIssueTypeName)) {
                //get issue type screen scheme name for project
                def isss = schemeManager.getIssueTypeScreenScheme(project)
                itscreenschemesFound.add(isss.getName())
                def isssEntities = isss.getEntities()
                def isssueTypeId = issuetype.getId()
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
                            screensFound.add(it.getFieldScreen())
                            projectsData.add("${project.getKey()},${project.getLeadUserName()},${project.getLeadUserKey()},${it.getFieldScreen().getName()}")
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
                                projectsData.add("${project.getKey()},${project.getLeadUserName()},${project.getLeadUserKey()},${it.getFieldScreen().getName()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
html += "<h2>Screens for $issueTypes for categories $categories</h2>"
//output unique list of screens
(screensFound.unique().sort{it.name}).each{  FieldScreen fieldScreen ->
    def screeenName = StringEscapeUtils.escapeCsv(fieldScreen.getName())
    html += "${screeenName}, id:${fieldScreen.getId()}<br/>"
    def tabs = fieldScreen.getTabs()
    tabs.each { FieldScreenTab tab ->
        def tabName = StringEscapeUtils.escapeCsv(tab.name)
      //  html +=  "<h3>" + tabName + "</h3>"
        def items = tab.getFieldScreenLayoutItems()
        items.each { FieldScreenLayoutItem item ->
            if (item.getOrderableField()) {
              def fieldName = item.getOrderableField().name
              def field = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldName).getAt(0)
              //"Screen,Tab,Field Name,Field Key,Field Type"
              String str = "${screeenName},$tabName,${StringEscapeUtils.escapeCsv(fieldName)},${item.getOrderableField()?.id},${field ? field.getCustomFieldType()?.getName() : 'system'}"
              infoList << str
            }
        }
    }
   // html += it.getName() + "<br>"
}
html += "<hr><h2>IssueType Screen Schemes</h2>"
//output unique list of IssueType screenschemes
(itscreenschemesFound.unique().sort()).each{
    html += it.toString() + "<br>"
}
html += "<hr><h2>Screen Schemes</h2>"
//output unique list of screenschemes
(screenschemesFound.unique()).each{
    html += it.toString() + "<br>"
}
html +="<h3>Projects with screens:</h3>"
(projectsData.unique().sort()).each{
    html += ""+it + "<br>"
}
issueFile.withWriter { out ->
  infoList.each {
    out.println it
  }
}
return html