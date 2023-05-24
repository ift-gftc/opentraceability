package queries

import java.util.*
import models.identifiers.*
import models.events.*

class EPCISQueryResults {

    var Document: EPCISQueryDocument? = null

    var StackTrace: List<EPCISQueryStackTraceItem> = ArrayList<EPCISQueryStackTraceItem>()

    var Errors: List<EPCISQueryError> = ArrayList<EPCISQueryError>()

    fun Merge(results: EPCISQueryResults) {
        TODO("Not yet implemented")
    }
}
