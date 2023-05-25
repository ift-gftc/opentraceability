package queries
import mappers.EPCISDataFormat
import models.identifiers.*
import models.events.*
import java.net.URI
class EPCISQueryInterfaceOptions {
    var URL: URI? = null
    var Version: EPCISVersion = EPCISVersion.V2
    var Format: EPCISDataFormat = EPCISDataFormat.JSON
    var EnableStackTrace: Boolean = true
}
