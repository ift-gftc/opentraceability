package queries

import java.util.*
import models.events.EPCISQueryDocument

class EPCISQueryResults {

    var Document: EPCISQueryDocument? = null

    var StackTrace: ArrayList<EPCISQueryStackTraceItem> = ArrayList<EPCISQueryStackTraceItem>()

    var Errors: ArrayList<EPCISQueryError> = ArrayList<EPCISQueryError>()

    fun merge(results: EPCISQueryResults) {

        results.StackTrace.forEach { el->
            this.StackTrace.add(el)
        }

        results.Errors.forEach { el->
            this.Errors.add(el)
        }

        if (this.Document == null)
        {
            this.Document = results.Document;
        }
        else if (results.Document != null)
        {
            this.Document!!.merge(results.Document!!);
        }
    }
}
