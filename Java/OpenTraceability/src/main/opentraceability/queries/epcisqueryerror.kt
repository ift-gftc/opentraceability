package queries

import models.identifiers.*
import java.lang.reflect.Type

class EPCISQueryError {
    lateinit var Type: EPCISQueryErrorType
    var Details: String = ""
    var StackTraceItemID: String = ""
}
