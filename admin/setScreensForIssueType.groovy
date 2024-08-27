//set Screen Scheme for issue type
// for all projects in Categories
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity
import com.atlassian.jira.config.IssueTypeManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.*
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)
IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.issueTypeScreenSchemeManager

//replace with your categories
def List categories = ["some Category"]
def issueTypeName ="issueTypeName" //Test
def screenSchemeName = "screenSchemeName" //
//will store the found screens schemes
List  screensFound = []
def html = ""

//FieldScreenManager fieldScreenManager;
FieldScreenSchemeManager fieldScreenSchemeManager =ComponentAccessor.getFieldScreenSchemeManager()
//IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
ConstantsManager constantsManager =  ComponentAccessor.getConstantsManager()
// get required issue type
def issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager)
def issueType = issueTypeManager.getIssueTypes().find{it.name == issueTypeName}
assert issueType: "No such issue type with name: $issueTypeName"
// get required Screen Scheme
def fss = fieldScreenSchemeManager.getFieldScreenSchemes().find{it.name == screenSchemeName}
assert fss: "No such Screen Scheme with name: $screenSchemeName"
categories.each{ category ->
    def categoryId = ComponentAccessor.getProjectManager().getProjectCategoryObjectByNameIgnoreCase(category).getId()
     //iterate over all projects in category
    html +=  "<h1>$category</h1>"
    ComponentAccessor.getProjectManager().getProjectObjectsFromProjectCategory(categoryId).each{ project->
        if ( project.getIssueTypes().contains(issueType)) {
                //def itssObj = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(20340) //just for testing
            def itssObj = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project)
            IssueTypeScreenSchemeEntity itsse = itssObj.getEntity(issueType.id)
            if (itsse) {
                // update to correct ScreenScheme later
                log.debug("'${itssObj.getName()}' already has entitity:${itsse.getId()}")
            } else {
                // create new association between issue type and field screen scheme
                itsse = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, fieldScreenSchemeManager, constantsManager)
                itsse.setIssueTypeId(issueType.id)
                log.debug("'${itssObj.getName()}' adding entitity:${itsse.getId()}")
            }
            itsse.setFieldScreenScheme(fss)
            itssObj.addEntity(itsse);

            def itss = itssObj.getName()
            def screenscheme = itssObj.getEffectiveFieldScreenScheme(issueType).name
            html +=  "${project.getName()} (${project.getKey()}), issue type screen scheme:  $itss , screenscheme for issuetype '$issueTypeName' :$screenscheme<br>"
            screensFound.add(itss)
        }
        else {
            log.debug("project: ${project.getKey()}  does not have issue type: $issueTypeName")
        }
    }
}
html += "<h3>Schemes:</h3>"+screensFound.unique().sort().join('<br>')
return html