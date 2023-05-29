package models.events

import interfaces.IEventKDE
import java.util.*
import models.events.*
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityExtensionElementsAttribute
import utility.attributes.OpenTraceabilityObjectAttribute

class SensorElement {

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorMetadata", 0)
    var MetaData: SensorMetaData? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","sensorReport", 1)
    var Reports: ArrayList<SensorReport> = ArrayList<SensorReport>()

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
