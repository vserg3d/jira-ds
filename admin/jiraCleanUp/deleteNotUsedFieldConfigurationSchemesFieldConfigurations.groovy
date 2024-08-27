import com.atlassian.jira.component.ComponentAccessor
def html = ''
def flm = ComponentAccessor.getFieldLayoutManager()
// Field Configuration Schemes without projects  - can be deleted
def schemesToDelete = flm.getFieldLayoutSchemes().findAll{!it.getProjectsUsing()}
html +="Field Configuration Schemes for deletion(count:${schemesToDelete.size()}):"+schemesToDelete*.name+'<br>'
//delete not used schemes
schemesToDelete.each{ scheme ->
   // flm.removeFieldLayoutScheme(scheme)
   html += "Scheme: ${scheme.name} was deleted<br>"
}
// part 2: deletion of Field Configurations
def layouts = flm.getEditableFieldLayouts()
// Field Configurations without Field Configuration Schemes - can be deleted
def fieldConfigurationsToDelete = layouts.findAll{!flm.getFieldConfigurationSchemes(it)}
html +="Field Configurations for deletion(count:${fieldConfigurationsToDelete.size()}):"+fieldConfigurationsToDelete*.name+'<br>'
//delete not used Field Configurations
fieldConfigurationsToDelete.each{ config ->
    //flm.deleteFieldLayout(config)
    html += "Config: ${config.name} was deleted<br>"
}
return html