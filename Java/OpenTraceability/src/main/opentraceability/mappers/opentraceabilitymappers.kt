package opentraceability.mappers

import opentraceability.models.identifiers.*
import opentraceability.models.events.*

class OpenTraceabilityMappers {
    companion object {
        var EPCISDocument: EPCISDocumentMappers = EPCISDocumentMappers()
        var EPCISQueryDocument: EPCISQueryDocumentMappers = EPCISQueryDocumentMappers()
        var MasterData: MasterDataMappers = MasterDataMappers()
    }
}
