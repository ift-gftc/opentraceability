package opentraceability.models.events

import opentraceability.models.identifiers.GLN
import opentraceability.utility.attributes.OpenTraceabilityAttribute

class EventLocation {

    @OpenTraceabilityAttribute("","id")
    var gln: GLN? = null

    constructor(gln: GLN) {
        this.gln = gln;
    }

}
