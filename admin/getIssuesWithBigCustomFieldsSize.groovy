import com.atlassian.jira.component.ComponentAccessor
import utils.Common
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.fields.screen.FieldScreen
import java.lang.Integer
import org.apache.commons.text.StringEscapeUtils
import org.apache.log4j.Level
import org.apache.log4j.Logger

def log = Logger.getLogger("com.vlas.admin")
log.setLevel(Level.DEBUG)

List infoList=["Issue,FieldName,FieldKey,FieldType,FieldValueSize,Project"]
Integer row=0
def issueFile = new File('opt/jira.home/scripts/temp','issues_oversized_fields_csv.groovy')
String str=""
def systemFields = ['description','environment']
def excludeFields = ['Case']
def lengthLimit = 32700
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager()
Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens()
def fieldsList = customFieldManager.getCustomFieldObjects().findAll(){'Text Field (multi-line)'==it.customFieldType.name && !excludeFields.contains(it.getName())}  // 'Testing Scenario'==it.getName()
def html=infoList.toString()+"<br>"

fieldsList.each() { field ->
    def jqlStr= "cf[${field.getId().substring(12)}] is not EMPTY"
    log.debug("field: $field")
    def jqlResult = Common.getIssuesFromJQL(jqlStr).findAll { it.getCustomFieldValue(field)?.length()>lengthLimit }
    jqlResult.each { issue ->
        String  project =issue.getProjectObject().key
        str="${issue.key},${StringEscapeUtils.escapeCsv(field.getName())},${field.getId() },${field.getCustomFieldType().getName() },${issue.getCustomFieldValue(field).length()},$project" //$screens,
        html += str+"<br>"
        infoList << str
    }


}
systemFields.each { sysField ->
    def jqlStr= "$sysField is not EMPTY"
    def sysText=''
    def jqlResult = Common.getIssuesFromJQL(jqlStr)
    jqlResult.each { issue ->
        String  project =issue.getProjectObject().key
        if ('description' == sysField) {
            sysText=issue.getDescription()
        } else {
            sysText=issue.getEnvironment()
        }
        def textLength =sysText.length()
        if ( textLength>lengthLimit ) {
            str="${issue.key},$sysField,$sysField, system,${textLength},$project"
            html += str+"<br>"
            infoList << str
        }

    }
}
issueFile.withWriter { out ->
  infoList.each {
    out.println it
  }
}
return html
