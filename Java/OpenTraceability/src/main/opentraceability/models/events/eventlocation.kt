package models.events

import models.identifiers.GLN

class EventLocation {
    //TODO: review this


    //[OpenTraceability("id")]
    var GLN: GLN? = null

    constructor(gln: GLN) {
        this.GLN = gln;
    }

}
