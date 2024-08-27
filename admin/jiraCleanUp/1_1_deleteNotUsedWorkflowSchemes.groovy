import com.atlassian.jira.component.ComponentAccessor

def schemeManager = ComponentAccessor.workflowSchemeManager

def sb = new StringBuffer()
def i=0
schemeManager.schemeObjects.each {
 try{
   if(schemeManager.getProjectsUsing(schemeManager.getWorkflowSchemeObj(it.id)).size() == 0) {
     schemeManager.deleteScheme(it.id)
     sb.append("${it.name}\n")
     i++
   }
 }
 catch(Exception e) {
   //noop
   sb.append("Error: " + e + "\n");
 }
}
sb.insert(0,"Deleted inactive workflow schemes ($i):\n")
return "<pre>" + sb.toString() + "<pre>"