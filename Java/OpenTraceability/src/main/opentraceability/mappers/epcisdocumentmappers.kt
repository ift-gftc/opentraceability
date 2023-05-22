package mappers

import interfaces.IEPCISDocumentMapper
import models.identifiers.*
import models.events.*

class EPCISDocumentMappers {
    var XML: IEPCISDocumentMapper = EPCISDocumentXMLMapper()
    var JSON: IEPCISDocumentMapper = EPCISDocumentJsonMapper()
}
