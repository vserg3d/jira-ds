import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Level
import org.apache.log4j.Logger


log = Logger.getLogger("com.vlas")
log.setLevel(Level.DEBUG)

List projects  = ['PROJECTKEY1','PROJECTKEY2' ]   //   -   projects, which will be deleted

def html = ""
ProjectService ps = ComponentAccessor.getComponent(ProjectService)
ApplicationUser user =ComponentAccessor.userManager.getUserByName('user who has privileges for deletion')
projects.each { project->
    def x = ComponentAccessor.getProjectManager().getProjectObjByKey(project)
    if (x) {
        ErrorCollection e = new SimpleErrorCollection();
        log.debug ('Validating ' + x.toString ());
        html +="Start with: $project. Validating: " + x.toString ()+'<br>';
        def dpvr =  new ProjectService.DeleteProjectValidationResult (e, x);
        log.debug ('Validated, deleting ' + x.toString ());
        html +='Validated, deleting ' + x.toString ()+'<br>';
        try {
            ps.deleteProject (user, dpvr);
            log.debug ('Deleted ' + x.toString ());
            html +='Deleted ' + x.toString () +'<br>';
        } catch (Exception ex) {
            log.debug ('Error deleting ' + x.toString ());
            html +='Error deleting ' + x.toString () +'<br>';
            log.debug (ex.getMessage());
            html +=ex.getMessage() +'<br>';
        }
    } else {
        html +="Warning! Project $project is absent <br>";
    }

}
return html