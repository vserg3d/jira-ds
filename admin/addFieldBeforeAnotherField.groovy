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
import com.atlassian.jira.issue.fields.FieldManager
import com.atlassian.jira.issue.fields.OrderableField

def fieldWithPositionName=  "FIELDONSCREENSNAME"
def fieldToInsertName = "NEWFIELDNAME"
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
FieldManager fieldManager = ComponentAccessor.getFieldManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
def html=''
def str=''
def fieldWithPositionId = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldWithPositionName).getAt(0)?.id
def fieldToInsert = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldToInsertName).getAt(0)
// if field is system:
// OrderableField fieldToInsert = fieldManager.getOrderableField("system name"); // description, summary, etc.
assert fieldToInsert: "Field name for insert: $fieldToInsertName is incorrect"
def screens =''
fieldScreens.each { fieldScreen ->

  List<FieldScreenTab> tabs = fieldScreen.getTabs()
  tabs.each { tab ->
   // html += tab.getName()+','+tab.getPosition()+"<br>"

    if (tab.isContainsField(fieldWithPositionId) && !fieldScreen.containsField(fieldToInsert.id)) {
     // fieldScreen.removeFieldScreenLayoutItem(fieldWithPositionId)
       tab.addFieldScreenLayoutItem(fieldToInsert.id,tab.getFieldScreenLayoutItem(fieldWithPositionId).getPosition())
       screens += "screen: ${fieldScreen.name}, tab: ${tab.name}, ${tab.id},Position:${tab.getFieldScreenLayoutItem(fieldWithPositionId).getPosition()}'<br>"
    }
  }
}
screens =StringEscapeUtils.escapeCsv(screens)

str="${fieldToInsert.getName()},${fieldToInsert.getId() },${fieldToInsert.getCustomFieldType().getName() }<br>$screens"
html += str+"<br>"

return html