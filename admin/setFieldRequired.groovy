import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)


def fieldName = "FIELD WAS REQUIRED"
def fieldRequiredName = "FIELD BECOMES REQUIRED"
def html = "$fieldName was required on Field Configurations:<br>"
def fieldConfigSchemeManager = ComponentAccessor.fieldConfigSchemeManager
def flm = ComponentAccessor.fieldLayoutManager
def layouts = flm.getEditableFieldLayouts()
def fieldId = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldName).getAt(0)?.id
def fieldSetRequiredId = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName(fieldRequiredName).getAt(0)?.id
layouts.each {
    def fieldLayout = it.getFieldLayoutItem(fieldId)
    def fieldSetRequiredLayout = it.getFieldLayoutItem(fieldSetRequiredId)
    if (fieldLayout.isRequired()) {
        html += "${it.getName()}<br>"
        it.makeRequired(fieldSetRequiredLayout)
        it.makeOptional(fieldLayout)
        flm.storeEditableFieldLayout(it)
        html += "${fieldSetRequiredLayout.getOrderableField().name} was set as required <br>"
    }
}
return html