package queries
import mappers.EPCISDataFormat
import models.identifiers.*
import models.events.*
import java.net.URI
class EPCISQueryInterfaceOptions {
    var URL: URI? = null
    var Version: EPCISVersion = EPCISVersion()
    var Format: EPCISDataFormat = EPCISDataFormat()
    var EnableStackTrace: Boolean = false
    companion object{
    }
}
