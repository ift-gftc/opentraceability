package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IVocabularyElement
import javax.xml.bind.annotation.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.events.EPCISBaseDocument

class EPCISXmlMasterDataWriter {
    companion object {

        fun WriteMasterData(xDocument: XmlElement, doc: EPCISBaseDocument) {
            TODO("Not yet implemented")
        }

        internal fun WriteMasterDataList(data: ArrayList<IVocabularyElement>, xVocabList: XmlElement, type: String) {
            TODO("Not yet implemented")
        }
        internal fun WriteMasterDataObject(md: IVocabularyElement) : XmlElement {
            TODO("Not yet implemented")
        }

        internal fun WriteObject(x: XmlElement, type: String, o: Object) {
            TODO("Not yet implemented")
        }
    }
}
