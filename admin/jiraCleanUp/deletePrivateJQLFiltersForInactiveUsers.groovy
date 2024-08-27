import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.bc.user.search.UserSearchParams
import com.atlassian.jira.bc.user.search.UserSearchService
import org.apache.commons.text.StringEscapeUtils
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("com.vlas.jqldata")
log.setLevel(Level.DEBUG)
log.warn(" running ...")
List infoList=["owner,private,filterId,filterName,permission,jql1,jql2"]
// Change this constant to the string you want to search for
String CUSTOM_FIELD_NAME = '' //STRING TO SEARCH IN JQL

SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService.class)
UserSearchService userSearchService = ComponentAccessor.getComponent(UserSearchService)
def html = ''
def privateCount =0
def resultFile = new File('/opt/jira.home/scripts/temp','filtersData_csv.groovy')
String str=""
UserSearchParams userSearchParams = new UserSearchParams.Builder()
    .allowEmptyQuery(true)
    .includeInactive(true)  // include inactive users (ordinary false is OK)
    .ignorePermissionCheck(true)
    .build()

//iterate over each users filters
// '[X]' - inactive users, to get all use empty ''
userSearchService.findUsers("[X]", userSearchParams).each{ApplicationUser filter_owner ->
    JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(filter_owner)
    try {
        searchRequestService.getOwnedFilters(filter_owner).each{SearchRequest filter->
                String jql1 = StringEscapeUtils.escapeCsv(filter.getQuery().getQueryString())
                String jql2 = StringEscapeUtils.escapeCsv(filter.getQuery().toString())
                def perms = StringEscapeUtils.escapeCsv(filter.permissions.getPermissionSet().toString())
              //  def perms2 = StringEscapeUtils.escapeCsv(filter.permissions.toString())
                def isPrivate =filter.getPermissions().isPrivate() ? 'Private' : 'Shared'
                if ('Private'==isPrivate) {
                    privateCount++
                    //if filter is private it can be deleted for inactive users
                        //additional check  - get JQL for each fiilter and check if it contains some string
                        // if (!CUSTOM_FIELD_NAME || jql2.contains(CUSTOM_FIELD_NAME)) {
                        //     delete fliter here
                        // }
                    //searchRequestService.validateForDelete(serviceContext,filter.id)
                    //searchRequestService.deleteFilter(serviceContext,filter.id);
                }
                str= "${StringEscapeUtils.escapeCsv(filter_owner.displayName)},${isPrivate}," +
                   "${filter.id},${StringEscapeUtils.escapeCsv(filter.name)},$perms,$jql1,$jql2"
                infoList << str
        }
    } catch (Exception e) {
        str = "Unable to get filters for ${filter_owner.displayName} due to ${e}<br>"
        infoList << str
        html +=str
    }
}
resultFile.withWriter { out ->
  infoList.each {
    out.println it
  }
}
html =  "total rows written to file: ${infoList.size()}. private: $privateCount <br>"+ html
return html
//output results
return