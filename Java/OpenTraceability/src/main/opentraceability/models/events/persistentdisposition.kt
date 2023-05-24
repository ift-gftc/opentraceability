package models.events
import java.util.*
import java.net.URI

//TODO: review this

class PersistentDisposition {

    //[OpenTraceability("unset", 1)]
    //[OpenTraceabilityArray]
    var Unset: List<URI>? = null

    //[OpenTraceability("set", 2)]
    //[OpenTraceabilityArray]
    var Set: List<URI>? = null
}
