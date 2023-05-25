package queries

import java.util.*
import models.identifiers.*
import models.events.*

class EPCISQueryResults {

    var Document: EPCISQueryDocument? = null

    var StackTrace: ArrayList<EPCISQueryStackTraceItem> = ArrayList<EPCISQueryStackTraceItem>()

    var Errors: ArrayList<EPCISQueryError> = ArrayList<EPCISQueryError>()

    fun Merge(results: EPCISQueryResults) {
        TODO("Not yet implemented")
    }
}
