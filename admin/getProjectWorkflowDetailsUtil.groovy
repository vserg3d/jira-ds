// supporting script - move to separate folder?
import groovy.json.JsonOutput
import groovy.json.JsonOutput
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowManager
import org.apache.log4j.Level
import org.apache.log4j.Logger


log = Logger.getLogger("com.vlas.jirautil.getworflowdetails")
log.setLevel(Level.DEBUG)

def PROJECT_CATEGORY = "Grouped Projects"
def EXCLUSION_LIST = ["Project 1"]

def projectManager = ComponentAccessor.getProjectManager()
def workflowManager = ComponentAccessor.getWorkflowManager()
def workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager()

def projectCategory = projectManager.getProjectCategoryObjectByName(PROJECT_CATEGORY)
def projects = projectManager.getProjectsFromProjectCategory(projectCategory)
def projectWorkflowsList = []

projects.each { myProject ->

   //def myProject = projectManager.getProjectObjByKey(PROJECT_KEY)
   if(!EXCLUSION_LIST.contains(myProject.getKey())) {
      def scheme = workflowSchemeManager.getWorkflowScheme(myProject)
      def projectWorkflowIssueType = workflowSchemeManager.getWorkflowMap(myProject)
      log.debug(myProject.getKey())

      def projectMap = ["project":myProject.getKey()]
      def workflowList = []


      workflowManager.getWorkflowsFromScheme(scheme).unique().each { workflow ->
        def workflowIssueTypeIdMap = projectWorkflowIssueType.findAll {it.value == workflow.getName()}
        def workflowMap = [:]
        def statusList = []
        def issueTypeList = []


        workflowMap.'name' = workflow.getName()

        log.debug(ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(myProject).findAll {
          workflowIssueTypeIdMap.keySet().contains(it.getId())
          }*.getName())

        ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(myProject).findAll {
          workflowIssueTypeIdMap.keySet().contains(it.getId())
          }.each { issueType ->
              issueTypeList.add(["name":issueType.getName()])}

        if(issueTypeList.size() == 0) {
          log.debug("IssueTypeList is size 0")
          log.debug(workflowIssueTypeIdMap)

          ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(myProject).findAll {
          !projectWorkflowIssueType.keySet().contains(it.getId())
          }.each { issueType ->
              issueTypeList.add(["name":issueType.getName()])}
        }

        log.debug(issueTypeList)

        workflowMap.'issueType' = issueTypeList

        workflow.getLinkedStatusObjects().each { status ->
          statusList.add(["name":status.getName()])
        }
        workflowMap.'status' = statusList
        workflowList.add(workflowMap)
      }
      projectMap.'workflows' = workflowList
      projectWorkflowsList.add(projectMap)
   }
}
JsonOutput.toJson(projectWorkflowsList)
