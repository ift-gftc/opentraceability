package queries
import java.util.*
import models.identifiers.*
import java.net.URI
class EPCISQueryStackTraceItem {
    var ID: String = "5ae654d5-7ac5-4ba3-9a17-9c30c01c54ff"
    var Created: DateTime = DateTime()
    var ResponseStatusCode: HttpStatusCode? = null
    var RelativeURL: URI? = URI?()
    var RequestHeaders: List<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>> = ArrayList<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>>()
    var ResponseHeaders: List<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>> = ArrayList<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>>()
    var RequestBody: String = String()
    var ResponseBody: String = String()
    companion object{
    }
}
