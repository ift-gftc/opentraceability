package queries
import java.util.*
import models.identifiers.*
import models.events.*
class EPCISQueryResults {
    var Document: EPCISQueryDocument = EPCISQueryDocument()
    var StackTrace: List<EPCISQueryStackTraceItem> = ArrayList<EPCISQueryStackTraceItem>()
    var Errors: List<EPCISQueryError> = ArrayList<EPCISQueryError>()
    companion object{
    }
}
