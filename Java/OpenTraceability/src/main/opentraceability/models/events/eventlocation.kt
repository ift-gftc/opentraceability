package models.events

import models.identifiers.GLN
import utility.attributes.OpenTraceabilityAttribute

class EventLocation {

    @OpenTraceabilityAttribute("","id")
    var GLN: GLN? = null

    constructor(gln: GLN) {
        this.GLN = gln;
    }

}
