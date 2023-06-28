package opentraceability.queries

import opentraceability.mappers.EPCISDataFormat
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.events.EPCISVersion
import java.net.URI

class DigitalLinkQueryOptions {
    var URL: URI? = null
    var Version: EPCISVersion = EPCISVersion.V2
    var Format: EPCISDataFormat = EPCISDataFormat.JSON
    var EnableStackTrace: Boolean = false
}
