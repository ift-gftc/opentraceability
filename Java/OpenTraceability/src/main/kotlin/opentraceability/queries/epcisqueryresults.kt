package opentraceability.queries

import java.util.*
import opentraceability.models.events.EPCISQueryDocument

class EPCISQueryResults {

    var Document: EPCISQueryDocument? = null

    var StackTrace: MutableList<EPCISQueryStackTraceItem> = mutableListOf()

    var Errors: MutableList<EPCISQueryError> = mutableListOf()

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
