package queries
import com.google.type.DateTime
import com.intellij.javaee.web.model.xml.HttpStatusCode
import java.util.*
import models.identifiers.*
import java.net.URI
class EPCISQueryStackTraceItem {
    var ID: String = "5c2d2f57-47c2-4cd4-a835-6d52749f8507"
    var Created: DateTime = DateTime()
    var ResponseStatusCode: HttpStatusCode? = null
    var RelativeURL: URI? = null
    var RequestHeaders: List<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>> = ArrayList<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>>()
    var ResponseHeaders: List<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>> = ArrayList<System.Collections.Generic.KeyValuePair<String,System.Collections.Generic.IEnumerable<String>>>()
    var RequestBody: String = String()
    var ResponseBody: String = String()
    companion object{
    }
}
