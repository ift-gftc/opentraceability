package models.events

import interfaces.IEvent
import interfaces.IVocabularyElement
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import models.events.*
import java.time.OffsetDateTime

class EPCISDocument : EPCISBaseDocument() {

    fun ToEPCISQueryDocument(): EPCISQueryDocument{
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
