package queries

import java.util.*
import models.identifiers.*
import models.events.*
import org.intellij.markdown.lexer.push

class EPCISQueryResults {

    var Document: EPCISQueryDocument? = null

    var StackTrace: ArrayList<EPCISQueryStackTraceItem> = ArrayList<EPCISQueryStackTraceItem>()

    var Errors: ArrayList<EPCISQueryError> = ArrayList<EPCISQueryError>()

    fun Merge(results: EPCISQueryResults) {

        results.StackTrace.forEach { el->
            this.StackTrace.push(el)
        }

        results.Errors.forEach { el->
            this.Errors.push(el)
        }

        if (this.Document == null)
        {
            this.Document = results.Document;
        }
        else if (results.Document != null)
        {
            this.Document!!.Merge(results.Document!!);
        }
    }
}