/*'''''''''''''''''''''''''''''''''''''''''''''
!!THIS CODE DELETES ALL THE OPTIONS for SELECTED 
SELECT LIST CUSTOM FIELD!!!!!! 

!!TO BE USED ONLY FOR CLEARING OUT ALL OPTIONS!!
'''''''''''''''''''''''''''''''''''''''''''''*/

package utils

import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger(this.class.getName())
log.setLevel(Level.DEBUG)

def fieldName = 'FIELDDOESNTEXIST'
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager()
def OptionsField = customFieldManager.getCustomFieldObjectsByName(fieldName).getAt(0)
def fieldConfig = OptionsField.getRelevantConfig(com.atlassian.jira.issue.context.IssueContext.GLOBAL)
def allOptions = ComponentAccessor.optionsManager.getOptions(fieldConfig)
log.debug(allOptions)
return ComponentAccessor.optionsManager.removeCustomFieldOptions(OptionsField)