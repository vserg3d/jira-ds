package scripts
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger
import utils.Fields
log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)

//get new version from automation trigger
String cvNew = inputs.properties['inputs']['Jira.Version']['name']
//inputs.properties['inputs']['Automation.RawEvent'].each{ addMessage("${it.key} ->  ${it.value}.   ")}
//addMessage("Version: ${inputs.serializedEvent.properties.dump()}")
addMessage(" New version: $cvNew")
addMessage( Fields.addvalue("Affected Version",cvNew,1) )
if (cvNew.startsWith("10.")) {
    addMessage( Fields.addvalue("10 version",cvNew) )
}

// zephyr hint (not implemented): https://support.smartbear.com/zephyr-squad-server/docs/api/custom-fields.html#update-custom-field