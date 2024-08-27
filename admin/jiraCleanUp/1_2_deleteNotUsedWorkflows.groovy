import com.atlassian.jira.component.ComponentAccessor

def workflowManager = ComponentAccessor.workflowManager
def schemeManager = ComponentAccessor.workflowSchemeManager

def sb = new StringBuffer()
//sb.append("Deleted inactive workflows:\n")
def i=0
workflowManager.workflows.each {
    if(!it.systemWorkflow) {
        def schemes = schemeManager.getSchemesForWorkflow(it)
        if (schemes.size() == 0) {
            def draftSchemes = schemeManager.getSchemesForWorkflowIncludingDrafts(it)
            if (draftSchemes.size() == 0) {
              // workflowManager.deleteWorkflow(it)
              sb.append("${it.name}\n")
              i++
            } else {
              // prerequisite: clean drafts according to https://confluence.atlassian.com/jirakb/unable-to-delete-inactive-workflow-726368983.html
              sb.append("!!! ${it.name} is used by draft WF scheme\n")
            }


        }
    }
}
sb.insert(0,"Deleted inactive workflows ($i):\n")
return "<pre>" + sb.toString() + "<pre>"