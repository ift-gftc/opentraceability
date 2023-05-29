package models.events

import interfaces.IEventKDE
import java.util.*
import models.events.*

//TODO: review this

class SensorElement {

    //[OpenTraceabilityObject]
    //[OpenTraceability("sensorMetadata", 0)]
    var MetaData: SensorMetaData? = null

    //[OpenTraceabilityArray]
    //[OpenTraceabilityObject]
    //[OpenTraceability("sensorReport", 1)]
    var Reports: ArrayList<SensorReport> = ArrayList<SensorReport>()

    //[OpenTraceabilityExtensionElements]
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}