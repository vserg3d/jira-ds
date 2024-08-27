import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.bc.user.search.UserSearchParams
import com.atlassian.jira.bc.user.search.UserSearchService

// Change this constant to the string you want to search for
String CUSTOM_FIELD_NAME = 'STRING TO SEARCH IN JQL'

SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService.class)
UserSearchService userSearchService = ComponentAccessor.getComponent(UserSearchService)
def sb = new StringBuffer()
def row =0
UserSearchParams userSearchParams = new UserSearchParams.Builder()
    .allowEmptyQuery(true)
    .includeInactive(true)  // include inactive users (ordinary false is OK)
    .ignorePermissionCheck(true)
    .build()

//iterate over each users filters
// '[X]' - inactive users, to get all use empty ''
userSearchService.findUsers("[X]", userSearchParams).each{ApplicationUser filter_owner ->
    try {
        searchRequestService.getOwnedFilters(filter_owner).each{SearchRequest filter->
            String jql = filter.getQuery().toString()
            //for each fiilter, get JQL and check if it contains our string
           // if (jql.contains(CUSTOM_FIELD_NAME)) {
                //sb.append(" --: ${filter_owner.displayName}, ${filter.name}, ${filter.getPermissions().isPrivate() ? 'Private' : 'Shared'}, ${jql}\n<br>")
                //if filter is private it can be deleted for inactive users (!)
                if (filter.getPermissions().isPrivate()) row++
           // }
        }
    } catch (Exception e) {

        sb.append("Unable to get filters for ${filter_owner.displayName} due to ${e}<br>")
    }
}
//output results
return "rows:$row <br>"+sb.toString()