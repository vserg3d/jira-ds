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


def fieldName=  "FIELDONSCREENSNAME"
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
def html=''
def field = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldName).getAt(0)
assert field :"$fieldName was not found"
def fieldId = field.id
def screens =''
fieldScreens.each { fieldScreen ->
  List<FieldScreenTab> tabs = fieldScreen.getTabs()
  tabs.each { tab ->
    if (tab.isContainsField(fieldId) ) {
        def position = tab.getFieldScreenLayoutItem(fieldId).getPosition()
       // fieldScreen.removeFieldScreenLayoutItem(fieldId)
        screens += "screen: ${StringEscapeUtils.escapeCsv(fieldScreen.name)}, tab: ${tab.name}, ${tab.id},Position:${position}'<br>"
    }
  }
}

html += "Field: ${field.getName()},${field.getId() },${field.getCustomFieldType().getName() } removed from screens<br>$screens"

return html