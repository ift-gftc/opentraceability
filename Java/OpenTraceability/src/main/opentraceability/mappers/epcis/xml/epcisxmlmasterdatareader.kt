package mappers.epcis.xml

import interfaces.IVocabularyElement
import javax.xml.bind.annotation.*
import models.events.*
import models.events.EPCISBaseDocument
import java.lang.reflect.Type

class EPCISXmlMasterDataReader {
    companion object {

        fun ReadMasterData(doc: EPCISBaseDocument, xMasterData: XmlElement) {
            TODO("Not yet implemented")
        }

        internal fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: XmlElement, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadLocation(doc: EPCISBaseDocument, xLocation: XmlElement, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadTradingParty(doc: EPCISBaseDocument, xTradingParty: XmlElement, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: XmlElement, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadMasterDataObject(md: IVocabularyElement, xMasterData: XmlElement, readKDEs: Boolean = true) {
            TODO("Not yet implemented")
        }

        internal fun ReadKDEObject(xeAtt: XmlElement, t: Type):Object {
            TODO("Not yet implemented")
        }

        //TODO: PropertyInfo p
        internal fun TrySetValueType(value: String, p: Type, o: Object):Boolean {
            TODO("Not yet implemented")
        }
    }
}
