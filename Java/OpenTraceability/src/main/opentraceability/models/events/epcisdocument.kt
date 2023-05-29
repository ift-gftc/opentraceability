package opentraceability.models.events

import opentraceability.interfaces.IEvent
import opentraceability.interfaces.IVocabularyElement
import opentraceability.models.common.StandardBusinessDocumentHeader
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import java.time.OffsetDateTime

class EPCISDocument : EPCISBaseDocument() {

    fun ToEPCISQueryDocument(): EPCISQueryDocument {
        var document: EPCISQueryDocument = EPCISQueryDocument()

        //TODO: Not yet implemented
        /*
            var props = typeof(EPCISBaseDocument).GetProperties();
            foreach (var p in props)
            {
                var v = p.GetValue(this);
                p.SetValue(document, v);
            }
         */

        return document
    }

}
