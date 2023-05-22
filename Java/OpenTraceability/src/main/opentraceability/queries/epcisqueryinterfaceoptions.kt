package queries
import models.identifiers.*
import models.events.*
import java.net.URI
class EPCISQueryInterfaceOptions {
    var URL: URI? = URI?()
    var Version: EPCISVersion = EPCISVersion()
    var Format: EPCISDataFormat = EPCISDataFormat()
    var EnableStackTrace: Boolean = Boolean()
    companion object{
    }
}
