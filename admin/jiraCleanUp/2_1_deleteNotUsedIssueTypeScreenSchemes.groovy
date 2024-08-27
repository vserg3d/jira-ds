import com.atlassian.jira.component.ComponentAccessor

def schemeManager = ComponentAccessor.issueTypeScreenSchemeManager
def defaultScheme = schemeManager.defaultScheme;

def sb = new StringBuffer()
def i=0
schemeManager.issueTypeScreenSchemes.each {
   try{
      if(it == defaultScheme) {
      //do not delete the default scheme
         return;
      }

      if(it.projects.size() == 0) {
         //remove any associations with screen schemes
       // schemeManager.removeIssueTypeSchemeEntities(it);  //1

         //remove the issue type screen scheme
       // schemeManager.removeIssueTypeScreenScheme(it);     //2
        sb.append("${it.name}\n")
        i++
      }
   }
   catch(Exception e) {
      //noop
      sb.append("Error: " + e + "\n");
   }
}
sb.insert(0,"Deleted issue type screen schemes without associated projects ($i):\n")
return "<pre>" + sb.toString() + "<pre>"