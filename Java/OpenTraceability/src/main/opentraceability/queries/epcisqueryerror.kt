package queries
import models.identifiers.*
import java.lang.reflect.Type
class EPCISQueryError {
    var Type: EPCISQueryErrorType = EPCISQueryErrorType()
    var Details: String = ""
    var StackTraceItemID: String = ""
    companion object{
    }
}
