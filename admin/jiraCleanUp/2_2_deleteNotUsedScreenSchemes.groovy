import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager

def fssm = ComponentAccessor.getComponent(FieldScreenSchemeManager.class)
def itssm = ComponentAccessor.issueTypeScreenSchemeManager

def sb = new StringBuffer()
def i=0
fssm.fieldScreenSchemes.each { fss ->
 try {
  def itssCollection = itssm.getIssueTypeScreenSchemes(fss);

  // find field screen schemes that are still associated with deleted issues type screen schemes
  def allDeleted = true;
  itssCollection.each { itss ->
   if(itss != null) {
    allDeleted = false;
    return;
   }
  }

  //remove field screen schemes with no (valid) associated issue type screen schemes
  if(itssCollection.size() == 0 || allDeleted == true) {
   //remove association to any screens
  // fssm.removeFieldSchemeItems(fss);   // 1
   //remove field screen scheme
  // fssm.removeFieldScreenScheme(fss);   // 2
   sb.append("${fss.name}\n");
   i++
  }

 }
 catch(Exception e) {
  //noop
  sb.append("Error: " + e + "\n");
 }
}
sb.insert(0,"Deleted screen schemes with no associated issue type screen schemes ($i):\n")
return "<pre>" + sb.toString() + "<pre>"