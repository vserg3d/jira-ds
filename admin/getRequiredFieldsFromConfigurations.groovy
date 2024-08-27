import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)

def html = "Get required fields according Field Configurations:<br>Field Configuration,Field Required<br>"
def fieldConfigSchemeManager = ComponentAccessor.fieldConfigSchemeManager
def flm = ComponentAccessor.fieldLayoutManager
def layouts = flm.getEditableFieldLayouts()
// layouts = layouts.take(2)
layouts.each { layout ->
    def fieldLayouts = layout.getFieldLayoutItems()
    fieldLayouts.each { fieldLayout->
        if (fieldLayout.isRequired()) {
            html += "\"${layout.getName()}\",\"${fieldLayout.getOrderableField().name}\"<br>"
        }
    }
}
return html