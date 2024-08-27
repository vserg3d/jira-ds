import com.atlassian.jira.component.ComponentAccessor
import utils.Common
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenTab
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme
import java.lang.Integer
import org.apache.commons.text.StringEscapeUtils

// ad-hock script to receive data (screens, tabs, positions) where the fields are placed
// list of fields
def fields= [
  "SOMEFIELDNAME",
]
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
def fieldsList = customFieldManager.getCustomFieldObjects().findAll(){fields.contains(it.name)}
def html=''

fieldsList.each() { field ->
    def screens =''
    fieldScreens.each { fieldScreen ->
      List<FieldScreenTab> tabs = fieldScreen.getTabs()
      tabs.each { tab ->
        if (tab.isContainsField(field.id)) {
          screens += "['${fieldScreen.name}', '${tab.name}',${tab.getFieldScreenLayoutItem(field.id).getPosition()}]"
        }
      }
    }
    screens =StringEscapeUtils.escapeCsv(screens)

    def str="${field.getName()},${field.getId() },${field.getCustomFieldType().getName() }, screens:<br>$screens"
    html += str+"<br>"
}
return html