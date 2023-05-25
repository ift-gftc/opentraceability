package models.events

import java.net.URI

//TODO: review this

class PersistentDisposition {

    //[OpenTraceability("unset", 1)]
    //[OpenTraceabilityArray]
    var Unset: ArrayList<URI>? = null

    //[OpenTraceability("set", 2)]
    //[OpenTraceabilityArray]
    var Set: ArrayList<URI>? = null
}
