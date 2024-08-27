import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.sharing.SharePermissionImpl
import com.atlassian.jira.sharing.SharedEntity
import com.atlassian.jira.sharing.rights.ShareRights
import com.atlassian.jira.sharing.type.ShareType
import com.atlassian.jira.user.ApplicationUser
import org.apache.commons.text.StringEscapeUtils

//you need to find and update the filters user an system admin priviledged user in case the current user doesn't have permission to adjust a filter
ApplicationUser adminUser =ComponentAccessor.userManager.getUserByName('USERNAME')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(adminUser)
SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
def html = ''
def filters = searchRequestService.getOwnedFilters(adminUser).findAll { it.getName().contains('STRING TO SEARCH IN FILTER NAME') }
filters.each{SearchRequest filter->
   // searchRequestService.validateForDelete(serviceContext,filter.id)
   // searchRequestService.deleteFilter(serviceContext,filter.id);

    String jql = StringEscapeUtils.escapeCsv(filter.getQuery().toString())
    html +=" ${filter.name}, $jql, ${filter.getPermissions().isPrivate() ? 'Private' : 'Shared'},\"${filter.getPermissions().toString()}\"\n<br>"

}
return html