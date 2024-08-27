//the file contains preliminary versions of scripts.
//It means the scripts are not completed. Adapt according to your requests

//working part

import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.sharing.SharePermissionImpl
import com.atlassian.jira.sharing.SharedEntity
import com.atlassian.jira.sharing.rights.ShareRights
import com.atlassian.jira.sharing.type.ShareType
import com.atlassian.jira.user.ApplicationUser

//you need to find and update the filters user an system admin priviledged user in case the current user doesn't have permission to adjust a filter
ApplicationUser adminUser =ComponentAccessor.userManager.getUserByName('USER NAME')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(adminUser)
SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
def html = ''
def filters = searchRequestService.getOwnedFilters(adminUser)
filters.each{SearchRequest filter->
    String jql = filter.getQuery().toString() //Found: ${filter.getOwner()},
    html +=" ${filter.name}, ${filter.getPermissions().isPrivate() ? 'Private' : 'Shared'}, \"${filter.getPermissions().toString()}\"\n<br>"
    //get all the current permission (but no the view permission, this will be replaced by the Authenticated one)
    // def perms = filter.permissions.permissionSet.collect().findAll{it.rights != ShareRights.VIEW}
    // //the new permission we will be adding
    // def authenticatedPerm = new SharePermissionImpl(null, ShareType.Name.AUTHENTICATED,null,null, ShareRights.VIEW)
    // perms.add( authenticatedPerm)
    // filter.setPermissions(new SharedEntity.SharePermissions(perms.toSet()))
    // searchRequestService.updateFilter(serviceContext, filter)
}
return html
//end of working part

// drafts
// ---------------------------

import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.bc.user.search.UserSearchParams
import com.atlassian.jira.bc.user.search.UserSearchService

// Change this constant to the string you want to search for
String CUSTOM_FIELD_NAME = 'SUBSTRING'

SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService.class)
UserSearchService userSearchService = ComponentAccessor.getComponent(UserSearchService)
def sb = new StringBuffer()

UserSearchParams userSearchParams = new UserSearchParams.Builder()
    .allowEmptyQuery(true)
    .includeInactive(false)
    .ignorePermissionCheck(true)
    .build()


//iterate over each user's filters
userSearchService.findUsers("", userSearchParams).each{ApplicationUser filter_owner ->
    try {
        searchRequestService.getOwnedFilters(filter_owner).each{SearchRequest filter->
            String jql = filter.getQuery().toString()
            //for each fiilter, get JQL and check if it contains our string
            if (jql.contains(CUSTOM_FIELD_NAME)) {
                sb.append("Found: ${filter_owner.displayName}, ${filter.name}, ${filter.getPermissions().isPrivate() ? 'Private' : 'Shared'}, ${jql}\n")
            }
        }
    } catch (Exception e) {
            //if filter is private
           sb.append("Unable to get filters for ${filter_owner.displayName} due to ${e}")
    }
}

//output results
return sb.toString()

import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.sharing.SharePermissionImpl
import com.atlassian.jira.sharing.SharedEntity
import com.atlassian.jira.sharing.rights.ShareRights
import com.atlassian.jira.sharing.type.ShareType
import com.atlassian.jira.user.ApplicationUser

//you need to find and update the filters user an system admin priviledged user in case the current user doesn't have permission to adjust a filter
ApplicationUser adminUser =ComponentAccessor.userManager.getUserByName('jira_superuser')//  ComponentAccessor.jiraAuthenticationContext.loggedInUser
JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(adminUser)
SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService)

def filtersToChange= [21525] //list all the filters you want to change

filtersToChange.each{filterId ->
    def filter = searchRequestService.getFilter(serviceContext, filterId)
    //get all the current permission (but no the view permission, this will be replaced by the Authenticated one)
    def perms = filter.permissions.permissionSet.collect().findAll{it.rights != ShareRights.VIEW}
    //the new permission we will be adding
    def authenticatedPerm = new SharePermissionImpl(null, ShareType.Name.AUTHENTICATED,null,null, ShareRights.VIEW)
    perms.add( authenticatedPerm)

    filter.setPermissions(new SharedEntity.SharePermissions(perms.toSet()))
    searchRequestService.updateFilter(serviceContext, filter)
}
