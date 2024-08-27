import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.FieldScreenFactory
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager
import com.atlassian.jira.bc.issue.fields.screen.FieldScreenService
import com.atlassian.jira.web.action.admin.issuefields.screens.ViewFieldScreens
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.webresource.api.assembler.PageBuilderService

def fieldScreenManager = ComponentAccessor.getFieldScreenManager()
def fieldScreenFactory = ComponentAccessor.getComponent(FieldScreenFactory.class)
def fieldScreenSchemeManager = ComponentAccessor.getComponent(FieldScreenSchemeManager.class)
def fieldScreenService = ComponentAccessor.getComponent(FieldScreenService.class)
def workflowManager = ComponentAccessor.getWorkflowManager()
def authenticationContext = ComponentAccessor.getJiraAuthenticationContext()
def pageBuilderService = ComponentAccessor.getComponent(PageBuilderService.class)

def viewFieldScreens = new ViewFieldScreens(fieldScreenManager, fieldScreenFactory, fieldScreenSchemeManager, fieldScreenService,
  workflowManager, authenticationContext, pageBuilderService)

// use StringBuffer to spit out log to screen for ScriptRunner Console
def sb = new StringBuffer()
def i=0
fieldScreenManager.getFieldScreens().each {
  fieldScreen ->

    //find all screens with no (or only null/previously deleted) screen schemes or workflows
    def allEmptyOrNull = true;

  viewFieldScreens.getFieldScreenSchemes(fieldScreen).each {
    fieldScreenScheme ->
      if (fieldScreenScheme != null) {
        allEmptyOrNull = false;
        return;
      }
  }
  if (!allEmptyOrNull) {
    return;
  }
  viewFieldScreens.getWorkflows(fieldScreen).each {
    workflow ->
      if (workflow != null) {
        allEmptyOrNull = false;
        return;
      }
  }
  if (allEmptyOrNull) {
   // fieldScreenManager.removeFieldScreen(fieldScreen.getId())
    sb.append("${fieldScreen.getName()}\n")
    i++
  }
}
sb.insert(0,"Deleted unused screens($i):\n")
return "<pre>" + sb.toString() + "<pre>"