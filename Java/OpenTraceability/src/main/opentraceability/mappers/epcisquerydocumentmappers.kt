package mappers

import interfaces.IEPCISDocumentMapper
import interfaces.IEPCISQueryDocumentMapper
import models.identifiers.*
import models.events.*

class EPCISQueryDocumentMappers {
    var XML: IEPCISQueryDocumentMapper = EPCISQueryDocumentXMLMapper()
    var JSON: IEPCISQueryDocumentMapper = EPCISQueryDocumentJsonMapper()
}
